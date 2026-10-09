package com.studentplansystem.studyplangym.service;

import com.studentplansystem.studyplangym.dto.PersonalStudyPlanResponse;
import com.studentplansystem.studyplangym.dto.StudyPlanGenerationRequest;
import com.studentplansystem.studyplangym.dto.StudySessionResponse;
import com.studentplansystem.studyplangym.entity.*;
import com.studentplansystem.studyplangym.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class StudyPlanGeneratorService {

    private final UserRepository userRepository;
    private final CourseDeadlineRepository deadlineRepository;
    private final StudentAvailabilityRepository availabilityRepository;
    private final StudentPreferenceRepository preferenceRepository;
    private final StudentGoalRepository goalRepository;
    private final PersonalStudyPlanRepository planRepository;
    private final StudySessionRepository sessionRepository;

    public StudyPlanGeneratorService(
            UserRepository userRepository,
            CourseDeadlineRepository deadlineRepository,
            StudentAvailabilityRepository availabilityRepository,
            StudentPreferenceRepository preferenceRepository,
            StudentGoalRepository goalRepository,
            PersonalStudyPlanRepository planRepository,
            StudySessionRepository sessionRepository
    ) {
        this.userRepository = userRepository;
        this.deadlineRepository = deadlineRepository;
        this.availabilityRepository = availabilityRepository;
        this.preferenceRepository = preferenceRepository;
        this.goalRepository = goalRepository;
        this.planRepository = planRepository;
        this.sessionRepository = sessionRepository;
    }

    @Transactional
    public PersonalStudyPlanResponse generatePlan(
            String username,
            StudyPlanGenerationRequest request
    ) {
        User user = getUser(username);

        validateRequest(request);

        StudentPreference preference =
                preferenceRepository
                        .findByUserId(user.getId())
                        .orElseGet(() -> {
                            StudentPreference created =
                                    new StudentPreference();
                            created.setUser(user);
                            return preferenceRepository.save(created);
                        });

        List<StudentAvailability> availability =
                availabilityRepository
                        .findByUserIdAndActiveTrueOrderByDayOfWeekAscStartTimeAsc(
                                user.getId()
                        );

        if (availability.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Add at least one availability period before generating a plan."
            );
        }

        List<CourseDeadline> deadlines =
                deadlineRepository
                        .findByStudentCourseUserIdAndStatusOrderByDueAtAsc(
                                user.getId(),
                                "PENDING"
                        );

        if (deadlines.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Add at least one pending deadline before generating a plan."
            );
        }

        List<StudentGoal> activeGoals =
                goalRepository
                        .findByUserIdAndStatusOrderByTargetDateAsc(
                                user.getId(),
                                "ACTIVE"
                        );

        List<WorkItem> workItems =
                new ArrayList<>();

        for (CourseDeadline deadline : deadlines) {

            if (deadline.getDueAt()
                    .toLocalDate()
                    .isBefore(request.getStartDate())) {
                continue;
            }

            int estimatedMinutes =
                    deadline.getEstimatedMinutes() != null
                            ? deadline.getEstimatedMinutes()
                            : preference.getPreferredSessionMinutes();

            int completedMinutes =
                    sessionRepository
                            .findByDeadlineIdAndPlanUserIdAndStatus(
                                    deadline.getId(),
                                    user.getId(),
                                    "COMPLETED"
                            )
                            .stream()
                            .mapToInt(session ->
                                    session.getActualMinutes() != null
                                            ? session.getActualMinutes()
                                            : session.getPlannedMinutes()
                            )
                            .sum();

            int remainingMinutes =
                    Math.max(
                            0,
                            estimatedMinutes - completedMinutes
                    );

            if (remainingMinutes <= 0) {
                continue;
            }

            StudentGoal relevantGoal =
                    findRelevantGoal(
                            deadline,
                            activeGoals
                    );

            workItems.add(
                    new WorkItem(
                            deadline,
                            relevantGoal,
                            remainingMinutes
                    )
            );
        }

        if (workItems.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "There is no remaining deadline workload to schedule in this planning period."
            );
        }

        PersonalStudyPlan plan =
                new PersonalStudyPlan();

        plan.setUser(user);

        plan.setPlanName(
                request.getPlanName() == null ||
                        request.getPlanName().isBlank()
                        ? "Personal Study Plan - " +
                        request.getStartDate()
                        : request.getPlanName().trim()
        );

        plan.setStartDate(request.getStartDate());
        plan.setEndDate(request.getEndDate());
        plan.setStatus("GENERATED");
        plan.setGenerationMethod("DETERMINISTIC");

        plan = planRepository.save(plan);

        List<StudySession> generatedSessions =
                generateSessions(
                        plan,
                        request,
                        preference,
                        availability,
                        workItems
                );

        int plannedMinutes =
                generatedSessions.stream()
                        .mapToInt(
                                StudySession::getPlannedMinutes
                        )
                        .sum();

        int remainingMinutes =
                workItems.stream()
                        .mapToInt(
                                item -> item.remainingMinutes
                        )
                        .sum();

        long outsidePreferred =
                countOutsidePreferredSessions(
                        generatedSessions,
                        preference
                );

        StringBuilder summary =
                new StringBuilder();

        summary.append("Generated ")
                .append(generatedSessions.size())
                .append(" study sessions (")
                .append(formatMinutes(plannedMinutes))
                .append("). ");

        if (remainingMinutes == 0) {
            summary.append(
                    "All identified deadline workload was scheduled."
            );
        } else {
            summary.append(
                    formatMinutes(remainingMinutes)
            ).append(
                    " of workload could not fit within the selected availability and plan period."
            );
        }

        if (
                !"ANY".equalsIgnoreCase(
                        preference.getPreferredStudyPeriod()
                ) &&
                outsidePreferred > 0
        ) {
            summary.append(" ")
                    .append(outsidePreferred)
                    .append(
                            outsidePreferred == 1
                                    ? " session was"
                                    : " sessions were"
                    )
                    .append(
                            " scheduled outside the preferred "
                    )
                    .append(
                            preference
                                    .getPreferredStudyPeriod()
                                    .toLowerCase()
                    )
                    .append(
                            " period because preferred-period availability was insufficient."
                    );
        }

        plan.setSummary(summary.toString());
        plan = planRepository.save(plan);

        supersedeOtherCurrentPlans(
                user.getId(),
                plan.getId()
        );

        return toResponse(
                plan,
                generatedSessions
        );
    }

    @Transactional
    public PersonalStudyPlanResponse replan(
            String username,
            Long sourcePlanId,
            StudyPlanGenerationRequest request
    ) {
        User user = getUser(username);

        validateRequest(request);

        PersonalStudyPlan sourcePlan =
                planRepository
                        .findByIdAndUserId(
                                sourcePlanId,
                                user.getId()
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Source study plan not found."
                                )
                        );

        if ("SUPERSEDED".equalsIgnoreCase(
                sourcePlan.getStatus()
        )) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "This study plan has already been superseded by a newer plan."
            );
        }

        StudentPreference preference =
                preferenceRepository
                        .findByUserId(user.getId())
                        .orElseGet(() -> {
                            StudentPreference created =
                                    new StudentPreference();
                            created.setUser(user);
                            return preferenceRepository.save(created);
                        });

        List<StudentAvailability> availability =
                availabilityRepository
                        .findByUserIdAndActiveTrueOrderByDayOfWeekAscStartTimeAsc(
                                user.getId()
                        );

        if (availability.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Add at least one availability period before replanning."
            );
        }

        List<CourseDeadline> deadlines =
                deadlineRepository
                        .findByStudentCourseUserIdAndStatusOrderByDueAtAsc(
                                user.getId(),
                                "PENDING"
                        );

        if (deadlines.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No pending deadlines are available for replanning."
            );
        }

        List<StudentGoal> activeGoals =
                goalRepository
                        .findByUserIdAndStatusOrderByTargetDateAsc(
                                user.getId(),
                                "ACTIVE"
                        );

        List<WorkItem> workItems =
                new ArrayList<>();

        int recognisedCompletedMinutes = 0;
        int originalWorkloadMinutes = 0;

        for (CourseDeadline deadline : deadlines) {

            if (deadline.getDueAt()
                    .toLocalDate()
                    .isBefore(request.getStartDate())) {
                continue;
            }

            int estimatedMinutes =
                    deadline.getEstimatedMinutes() != null
                            ? deadline.getEstimatedMinutes()
                            : preference.getPreferredSessionMinutes();

            int completedMinutes =
                    sessionRepository
                            .findByDeadlineIdAndPlanUserIdAndStatus(
                                    deadline.getId(),
                                    user.getId(),
                                    "COMPLETED"
                            )
                            .stream()
                            .mapToInt(session ->
                                    session.getActualMinutes() != null
                                            ? session.getActualMinutes()
                                            : session.getPlannedMinutes()
                            )
                            .sum();

            int remainingMinutes =
                    Math.max(
                            0,
                            estimatedMinutes - completedMinutes
                    );

            originalWorkloadMinutes +=
                    estimatedMinutes;

            recognisedCompletedMinutes +=
                    Math.min(
                            completedMinutes,
                            estimatedMinutes
                    );

            if (remainingMinutes <= 0) {
                continue;
            }

            StudentGoal relevantGoal =
                    findRelevantGoal(
                            deadline,
                            activeGoals
                    );

            workItems.add(
                    new WorkItem(
                            deadline,
                            relevantGoal,
                            remainingMinutes
                    )
            );
        }

        if (workItems.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "There is no remaining deadline workload to replan."
            );
        }

        PersonalStudyPlan adaptivePlan =
                new PersonalStudyPlan();

        adaptivePlan.setUser(user);
        adaptivePlan.setSourcePlan(sourcePlan);

        adaptivePlan.setPlanName(
                request.getPlanName() == null ||
                        request.getPlanName().isBlank()
                        ? "Adaptive Study Plan - " +
                        request.getStartDate()
                        : request.getPlanName().trim()
        );

        adaptivePlan.setStartDate(
                request.getStartDate()
        );

        adaptivePlan.setEndDate(
                request.getEndDate()
        );

        adaptivePlan.setStatus(
                "GENERATED"
        );

        adaptivePlan.setGenerationMethod(
                "ADAPTIVE"
        );

        adaptivePlan =
                planRepository.save(
                        adaptivePlan
                );

        List<StudySession> generatedSessions =
                generateSessions(
                        adaptivePlan,
                        request,
                        preference,
                        availability,
                        workItems
                );

        int plannedMinutes =
                generatedSessions.stream()
                        .mapToInt(
                                StudySession::getPlannedMinutes
                        )
                        .sum();

        int remainingUnscheduledMinutes =
                workItems.stream()
                        .mapToInt(
                                item -> item.remainingMinutes
                        )
                        .sum();

        StringBuilder summary =
                new StringBuilder();

        summary.append(
                "Adaptive plan generated from plan #"
        )
                .append(sourcePlan.getId())
                .append(". Recognised ")
                .append(
                        formatMinutes(
                                recognisedCompletedMinutes
                        )
                )
                .append(
                        " of completed study time from "
                )
                .append(
                        formatMinutes(
                                originalWorkloadMinutes
                        )
                )
                .append(
                        " of identified workload. Generated "
                )
                .append(
                        generatedSessions.size()
                )
                .append(
                        " new study sessions ("
                )
                .append(
                        formatMinutes(
                                plannedMinutes
                        )
                )
                .append("). ");

        if (remainingUnscheduledMinutes == 0) {
            summary.append(
                    "All remaining workload was scheduled."
            );
        } else {
            summary.append(
                    formatMinutes(
                            remainingUnscheduledMinutes
                    )
            )
                    .append(
                            " of remaining workload could not fit within the selected availability and plan period."
                    );
        }

        adaptivePlan.setSummary(
                summary.toString()
        );

        adaptivePlan =
                planRepository.save(
                        adaptivePlan
                );

        supersedeOtherCurrentPlans(
                user.getId(),
                adaptivePlan.getId()
        );

        return toResponse(
                adaptivePlan,
                generatedSessions
        );
    }

    @Transactional
    public PersonalStudyPlanResponse getLatestPlan(
            String username
    ) {
        User user = getUser(username);

        PersonalStudyPlan plan =
                planRepository
                        .findFirstByUserIdOrderByGeneratedAtDesc(
                                user.getId()
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "No generated study plan found."
                                )
                        );

        List<StudySession> sessions =
                sessionRepository
                        .findByPlanIdOrderByStartAtAsc(
                                plan.getId()
                        );

        return toResponse(
                plan,
                sessions
        );
    }

    private List<StudySession> generateSessions(
            PersonalStudyPlan plan,
            StudyPlanGenerationRequest request,
            StudentPreference preference,
            List<StudentAvailability> availability,
            List<WorkItem> workItems
    ) {
        List<StudySession> sessions =
                new ArrayList<>();

        Map<LocalDate, Integer> dailyMinutesUsed =
                new HashMap<>();

        String preferredPeriod =
                preference.getPreferredStudyPeriod() == null
                        ? "ANY"
                        : preference
                                .getPreferredStudyPeriod()
                                .toUpperCase();

        /*
         * Phase 1:
         * Try to schedule everything inside the student's
         * preferred study period.
         */
        if (!"ANY".equals(preferredPeriod)) {
            scheduleAcrossRange(
                    plan,
                    request,
                    preference,
                    availability,
                    workItems,
                    sessions,
                    dailyMinutesUsed,
                    true
            );
        }

        /*
         * Phase 2:
         * If work is still remaining, use the rest of the
         * student's declared availability.
         *
         * This is why the study-period preference is soft
         * instead of a hard restriction.
         */
        if (hasRemainingWork(workItems)) {
            scheduleAcrossRange(
                    plan,
                    request,
                    preference,
                    availability,
                    workItems,
                    sessions,
                    dailyMinutesUsed,
                    false
            );
        }

        sessions.sort(
                Comparator.comparing(
                        StudySession::getStartAt
                )
        );

        return sessions;
    }

    private void scheduleAcrossRange(
            PersonalStudyPlan plan,
            StudyPlanGenerationRequest request,
            StudentPreference preference,
            List<StudentAvailability> availability,
            List<WorkItem> workItems,
            List<StudySession> sessions,
            Map<LocalDate, Integer> dailyMinutesUsed,
            boolean preferredPhase
    ) {
        LocalDate date =
                request.getStartDate();

        while (
                !date.isAfter(request.getEndDate()) &&
                hasRemainingWork(workItems)
        ) {
            final LocalDate currentDate =
                    date;

            List<StudentAvailability> daySlots =
                    availability.stream()
                            .filter(slot ->
                                    slot.getDayOfWeek() ==
                                            currentDate
                                                    .getDayOfWeek()
                                                    .getValue()
                            )
                            .toList();

            for (StudentAvailability slot : daySlots) {

                List<StudyWindow> windows =
                        buildStudyWindows(
                                currentDate,
                                slot,
                                preference,
                                preferredPhase
                        );

                for (StudyWindow window : windows) {

                    if (!hasRemainingWork(workItems)) {
                        break;
                    }

                    scheduleInsideWindow(
                            plan,
                            window,
                            preference,
                            workItems,
                            sessions,
                            dailyMinutesUsed,
                            preferredPhase
                    );
                }
            }

            date = date.plusDays(1);
        }
    }

    private void scheduleInsideWindow(
            PersonalStudyPlan plan,
            StudyWindow window,
            StudentPreference preference,
            List<WorkItem> workItems,
            List<StudySession> sessions,
            Map<LocalDate, Integer> dailyMinutesUsed,
            boolean preferredPhase
    ) {
        LocalDate date =
                window.start.toLocalDate();

        int usedToday =
                dailyMinutesUsed.getOrDefault(
                        date,
                        0
                );

        if (
                usedToday >=
                        preference.getMaximumDailyMinutes()
        ) {
            return;
        }

        if (window.scheduleBackward) {

            LocalDateTime cursorEnd =
                    window.end;

            while (
                    cursorEnd.isAfter(window.start) &&
                    hasRemainingWork(workItems)
            ) {
                usedToday =
                        dailyMinutesUsed.getOrDefault(
                                date,
                                0
                        );

                int remainingDaily =
                        preference
                                .getMaximumDailyMinutes()
                                - usedToday;

                if (remainingDaily <= 0) {
                    break;
                }

                WorkItem item =
                        chooseWorkItem(
                                workItems,
                                cursorEnd.minusNanos(1)
                        );

                if (item == null) {
                    break;
                }

                LocalDateTime effectiveEnd =
                        item.deadline
                                .getDueAt()
                                .isBefore(cursorEnd)
                                ? item.deadline.getDueAt()
                                : cursorEnd;

                long availableMinutes =
                        Duration.between(
                                window.start,
                                effectiveEnd
                        ).toMinutes();

                int sessionMinutes =
                        calculateSessionMinutes(
                                preference,
                                item,
                                remainingDaily,
                                availableMinutes
                        );

                if (sessionMinutes <= 0) {
                    break;
                }

                LocalDateTime sessionStart =
                        effectiveEnd.minusMinutes(
                                sessionMinutes
                        );

                StudySession session =
                        createSession(
                                plan,
                                item,
                                sessionStart,
                                effectiveEnd,
                                sessionMinutes,
                                preference,
                                preferredPhase
                        );

                sessions.add(session);

                item.remainingMinutes -=
                        sessionMinutes;

                dailyMinutesUsed.put(
                        date,
                        usedToday + sessionMinutes
                );

                cursorEnd =
                        sessionStart.minusMinutes(
                                preference.getBreakMinutes()
                        );
            }

        } else {

            LocalDateTime cursor =
                    window.start;

            while (
                    cursor.isBefore(window.end) &&
                    hasRemainingWork(workItems)
            ) {
                usedToday =
                        dailyMinutesUsed.getOrDefault(
                                date,
                                0
                        );

                int remainingDaily =
                        preference
                                .getMaximumDailyMinutes()
                                - usedToday;

                if (remainingDaily <= 0) {
                    break;
                }

                WorkItem item =
                        chooseWorkItem(
                                workItems,
                                cursor
                        );

                if (item == null) {
                    break;
                }

                LocalDateTime deadlineLimit =
                        item.deadline
                                .getDueAt()
                                .isBefore(window.end)
                                ? item.deadline.getDueAt()
                                : window.end;

                long availableMinutes =
                        Duration.between(
                                cursor,
                                deadlineLimit
                        ).toMinutes();

                int sessionMinutes =
                        calculateSessionMinutes(
                                preference,
                                item,
                                remainingDaily,
                                availableMinutes
                        );

                if (sessionMinutes <= 0) {
                    break;
                }

                LocalDateTime sessionEnd =
                        cursor.plusMinutes(
                                sessionMinutes
                        );

                StudySession session =
                        createSession(
                                plan,
                                item,
                                cursor,
                                sessionEnd,
                                sessionMinutes,
                                preference,
                                preferredPhase
                        );

                sessions.add(session);

                item.remainingMinutes -=
                        sessionMinutes;

                dailyMinutesUsed.put(
                        date,
                        usedToday + sessionMinutes
                );

                cursor =
                        sessionEnd.plusMinutes(
                                preference.getBreakMinutes()
                        );
            }
        }
    }

    private StudySession createSession(
            PersonalStudyPlan plan,
            WorkItem item,
            LocalDateTime start,
            LocalDateTime end,
            int sessionMinutes,
            StudentPreference preference,
            boolean preferredPhase
    ) {
        int score =
                calculatePriorityScore(
                        item,
                        start.toLocalDate()
                );

        StudySession session =
                new StudySession();

        session.setPlan(plan);

        session.setStudentCourse(
                item.deadline
                        .getStudentCourse()
        );

        session.setDeadline(
                item.deadline
        );

        session.setGoal(
                item.goal
        );

        session.setTitle(
                item.deadline
                        .getStudentCourse()
                        .getName()
                        + " — "
                        + item.deadline.getTitle()
        );

        session.setStartAt(start);
        session.setEndAt(end);
        session.setPlannedMinutes(
                sessionMinutes
        );
        session.setStatus("PLANNED");

        session.setRationale(
                buildRationale(
                        item,
                        start.toLocalDate(),
                        score,
                        preference,
                        preferredPhase
                )
        );

        return sessionRepository.save(
                session
        );
    }

    private int calculateSessionMinutes(
            StudentPreference preference,
            WorkItem item,
            int remainingDaily,
            long availableMinutes
    ) {
        int sessionMinutes =
                Math.min(
                        preference
                                .getPreferredSessionMinutes(),
                        preference
                                .getMaximumSessionMinutes()
                );

        sessionMinutes =
                Math.min(
                        sessionMinutes,
                        item.remainingMinutes
                );

        sessionMinutes =
                Math.min(
                        sessionMinutes,
                        remainingDaily
                );

        sessionMinutes =
                Math.min(
                        sessionMinutes,
                        (int) Math.max(
                                0,
                                availableMinutes
                        )
                );

        return sessionMinutes;
    }

    private List<StudyWindow> buildStudyWindows(
            LocalDate date,
            StudentAvailability slot,
            StudentPreference preference,
            boolean preferredPhase
    ) {
        LocalDateTime availabilityStart =
                LocalDateTime.of(
                        date,
                        slot.getStartTime()
                );

        LocalDateTime availabilityEnd =
                LocalDateTime.of(
                        date,
                        slot.getEndTime()
                );

        String preferredPeriod =
                preference.getPreferredStudyPeriod() == null
                        ? "ANY"
                        : preference
                                .getPreferredStudyPeriod()
                                .toUpperCase();

        if ("ANY".equals(preferredPeriod)) {

            if (preferredPhase) {
                return List.of();
            }

            return List.of(
                    new StudyWindow(
                            availabilityStart,
                            availabilityEnd,
                            false
                    )
            );
        }

        List<TimeRange> preferredRanges =
                getPreferredRanges(
                        preferredPeriod
                );

        if (preferredPhase) {

            List<StudyWindow> intersections =
                    new ArrayList<>();

            for (TimeRange range : preferredRanges) {

                LocalDateTime rangeStart =
                        LocalDateTime.of(
                                date,
                                range.start
                        );

                LocalDateTime rangeEnd =
                        LocalDateTime.of(
                                date,
                                range.end
                        );

                LocalDateTime start =
                        availabilityStart.isAfter(rangeStart)
                                ? availabilityStart
                                : rangeStart;

                LocalDateTime end =
                        availabilityEnd.isBefore(rangeEnd)
                                ? availabilityEnd
                                : rangeEnd;

                if (start.isBefore(end)) {
                    intersections.add(
                            new StudyWindow(
                                    start,
                                    end,
                                    false
                            )
                    );
                }
            }

            return intersections;
        }

        /*
         * Build only the parts of availability that are
         * outside the preferred period.
         *
         * This avoids scheduling twice in the same time block
         * after the preferred phase has already been processed.
         */
        List<StudyWindow> remaining =
                new ArrayList<>();

        remaining.add(
                new StudyWindow(
                        availabilityStart,
                        availabilityEnd,
                        false
                )
        );

        for (TimeRange range : preferredRanges) {

            LocalDateTime rangeStart =
                    LocalDateTime.of(
                            date,
                            range.start
                    );

            LocalDateTime rangeEnd =
                    LocalDateTime.of(
                            date,
                            range.end
                    );

            List<StudyWindow> next =
                    new ArrayList<>();

            for (StudyWindow window : remaining) {

                if (
                        rangeEnd.compareTo(window.start) <= 0 ||
                        rangeStart.compareTo(window.end) >= 0
                ) {
                    next.add(window);
                    continue;
                }

                if (window.start.isBefore(rangeStart)) {
                    next.add(
                            new StudyWindow(
                                    window.start,
                                    rangeStart,
                                    false
                            )
                    );
                }

                if (window.end.isAfter(rangeEnd)) {
                    next.add(
                            new StudyWindow(
                                    rangeEnd,
                                    window.end,
                                    false
                            )
                    );
                }
            }

            remaining = next;
        }

        List<StudyWindow> result =
                new ArrayList<>();

        for (StudyWindow window : remaining) {

            if (!window.start.isBefore(window.end)) {
                continue;
            }

            boolean backward =
                    shouldScheduleBackward(
                            window,
                            date,
                            preferredRanges
                    );

            result.add(
                    new StudyWindow(
                            window.start,
                            window.end,
                            backward
                    )
            );
        }

        return result;
    }

    private boolean shouldScheduleBackward(
            StudyWindow window,
            LocalDate date,
            List<TimeRange> preferredRanges
    ) {
        long beforeDistance =
                Long.MAX_VALUE;

        long afterDistance =
                Long.MAX_VALUE;

        for (TimeRange range : preferredRanges) {

            LocalDateTime preferredStart =
                    LocalDateTime.of(
                            date,
                            range.start
                    );

            LocalDateTime preferredEnd =
                    LocalDateTime.of(
                            date,
                            range.end
                    );

            if (
                    !window.end.isAfter(
                            preferredStart
                    )
            ) {
                beforeDistance =
                        Math.min(
                                beforeDistance,
                                Duration.between(
                                        window.end,
                                        preferredStart
                                ).toMinutes()
                        );
            }

            if (
                    !window.start.isBefore(
                            preferredEnd
                    )
            ) {
                afterDistance =
                        Math.min(
                                afterDistance,
                                Duration.between(
                                        preferredEnd,
                                        window.start
                                ).toMinutes()
                        );
            }
        }

        /*
         * If the availability lies before the preferred period,
         * schedule from the end of the slot backwards.
         *
         * Example:
         * Availability 00:00-17:00
         * Preference   EVENING (17:00-22:00)
         *
         * Fallback study will be placed near 17:00 rather
         * than unnecessarily beginning at midnight.
         */
        return beforeDistance <=
                afterDistance;
    }

    private List<TimeRange> getPreferredRanges(
            String preferredPeriod
    ) {
        return switch (preferredPeriod) {

            case "MORNING" ->
                    List.of(
                            new TimeRange(
                                    LocalTime.of(6, 0),
                                    LocalTime.of(12, 0)
                            )
                    );

            case "AFTERNOON" ->
                    List.of(
                            new TimeRange(
                                    LocalTime.of(12, 0),
                                    LocalTime.of(17, 0)
                            )
                    );

            case "EVENING" ->
                    List.of(
                            new TimeRange(
                                    LocalTime.of(17, 0),
                                    LocalTime.of(22, 0)
                            )
                    );

            case "NIGHT" ->
                    List.of(
                            new TimeRange(
                                    LocalTime.MIN,
                                    LocalTime.of(6, 0)
                            ),
                            new TimeRange(
                                    LocalTime.of(22, 0),
                                    LocalTime.MAX
                            )
                    );

            default ->
                    List.of();
        };
    }

    private WorkItem chooseWorkItem(
            List<WorkItem> workItems,
            LocalDateTime current
    ) {
        return workItems.stream()
                .filter(item ->
                        item.remainingMinutes > 0
                )
                .filter(item ->
                        current.isBefore(
                                item.deadline.getDueAt()
                        )
                )
                .max(
                        Comparator.comparingInt(
                                item ->
                                        calculatePriorityScore(
                                                item,
                                                current.toLocalDate()
                                        )
                        )
                )
                .orElse(null);
    }

    private int calculatePriorityScore(
            WorkItem item,
            LocalDate date
    ) {
        CourseDeadline deadline =
                item.deadline;

        StudentCourse course =
                deadline.getStudentCourse();

        long daysUntil =
                ChronoUnit.DAYS.between(
                        date,
                        deadline
                                .getDueAt()
                                .toLocalDate()
                );

        int urgency;

        if (daysUntil <= 2) {
            urgency = 20;
        } else if (daysUntil <= 7) {
            urgency = 15;
        } else if (daysUntil <= 14) {
            urgency = 10;
        } else if (daysUntil <= 30) {
            urgency = 5;
        } else {
            urgency = 0;
        }

        int goalBoost =
                item.goal != null
                        ? item.goal.getPriority() * 2
                        : 0;

        return deadline.getImportance() * 4
                + course.getPriority() * 3
                + course.getDifficulty() * 2
                + urgency
                + goalBoost;
    }

    private StudentGoal findRelevantGoal(
            CourseDeadline deadline,
            List<StudentGoal> goals
    ) {
        String courseName =
                deadline.getStudentCourse()
                        .getName()
                        .toLowerCase();

        String courseCode =
                deadline.getStudentCourse()
                        .getCode();

        Set<String> courseWords =
                new HashSet<>(
                        Arrays.asList(
                                courseName
                                        .split("[^a-z0-9]+")
                        )
                );

        return goals.stream()
                .filter(goal -> {

                    String text =
                            (
                                    goal.getTitle() +
                                    " " +
                                    (
                                            goal.getDescription() == null
                                                    ? ""
                                                    : goal.getDescription()
                                    )
                            ).toLowerCase();

                    if (
                            courseCode != null &&
                            !courseCode.isBlank() &&
                            text.contains(
                                    courseCode.toLowerCase()
                            )
                    ) {
                        return true;
                    }

                    return courseWords.stream()
                            .filter(word ->
                                    word.length() >= 5
                            )
                            .anyMatch(
                                    text::contains
                            );
                })
                .max(
                        Comparator.comparingInt(
                                StudentGoal::getPriority
                        )
                )
                .orElse(null);
    }

    private String buildRationale(
            WorkItem item,
            LocalDate date,
            int score,
            StudentPreference preference,
            boolean preferredPhase
    ) {
        long days =
                ChronoUnit.DAYS.between(
                        date,
                        item.deadline
                                .getDueAt()
                                .toLocalDate()
                );

        StringBuilder rationale =
                new StringBuilder();

        rationale.append("Priority score ")
                .append(score)
                .append(
                        ": deadline due in "
                )
                .append(days)
                .append(
                        " day(s), importance "
                )
                .append(
                        item.deadline.getImportance()
                )
                .append(
                        "/5, course priority "
                )
                .append(
                        item.deadline
                                .getStudentCourse()
                                .getPriority()
                )
                .append(
                        "/5, difficulty "
                )
                .append(
                        item.deadline
                                .getStudentCourse()
                                .getDifficulty()
                )
                .append("/5.");

        if (item.goal != null) {
            rationale.append(
                    " Related active goal: \""
            )
                    .append(
                            item.goal.getTitle()
                    )
                    .append(
                            "\" (priority "
                    )
                    .append(
                            item.goal.getPriority()
                    )
                    .append("/5).");
        }

        String period =
                preference.getPreferredStudyPeriod();

        if (
                period != null &&
                !"ANY".equalsIgnoreCase(period)
        ) {
            if (preferredPhase) {
                rationale.append(
                        " Scheduled within the student's preferred "
                )
                        .append(
                                period.toLowerCase()
                        )
                        .append(
                                " study period."
                        );
            } else {
                rationale.append(
                        " Scheduled outside the preferred "
                )
                        .append(
                                period.toLowerCase()
                        )
                        .append(
                                " period because additional study capacity was required."
                        );
            }
        }

        return rationale.toString();
    }

    private boolean hasRemainingWork(
            List<WorkItem> workItems
    ) {
        return workItems.stream()
                .anyMatch(
                        item ->
                                item.remainingMinutes > 0
                );
    }

    private long countOutsidePreferredSessions(
            List<StudySession> sessions,
            StudentPreference preference
    ) {
        String period =
                preference.getPreferredStudyPeriod();

        if (
                period == null ||
                "ANY".equalsIgnoreCase(period)
        ) {
            return 0;
        }

        return sessions.stream()
                .filter(session ->
                        !isWithinPreferredPeriod(
                                session.getStartAt(),
                                session.getEndAt(),
                                period
                        )
                )
                .count();
    }

    private boolean isWithinPreferredPeriod(
            LocalDateTime start,
            LocalDateTime end,
            String period
    ) {
        List<TimeRange> ranges =
                getPreferredRanges(
                        period.toUpperCase()
                );

        for (TimeRange range : ranges) {

            LocalTime startTime =
                    start.toLocalTime();

            LocalTime endTime =
                    end.toLocalTime();

            if (
                    !startTime.isBefore(range.start) &&
                    !endTime.isAfter(range.end)
            ) {
                return true;
            }
        }

        return false;
    }

    private void validateRequest(
            StudyPlanGenerationRequest request
    ) {
        if (request.getStartDate() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Plan start date is required."
            );
        }

        if (request.getEndDate() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Plan end date is required."
            );
        }

        if (
                request.getEndDate()
                        .isBefore(
                                request.getStartDate()
                        )
        ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Plan end date cannot be before start date."
            );
        }

        long days =
                ChronoUnit.DAYS.between(
                        request.getStartDate(),
                        request.getEndDate()
                );

        if (days > 30) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "The first planner version supports a maximum planning period of 31 days."
            );
        }
    }

    private User getUser(
            String username
    ) {
        return userRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Authenticated user could not be found."
                        )
                );
    }

    private PersonalStudyPlanResponse toResponse(
            PersonalStudyPlan plan,
            List<StudySession> sessions
    ) {
        List<StudySessionResponse> sessionResponses =
                sessions.stream()
                        .map(
                                this::toSessionResponse
                        )
                        .toList();

        return new PersonalStudyPlanResponse(
                plan.getId(),
                plan.getSourcePlan() != null
                        ? plan.getSourcePlan().getId()
                        : null,
                plan.getPlanName(),
                plan.getStartDate(),
                plan.getEndDate(),
                plan.getStatus(),
                plan.getGenerationMethod(),
                plan.getSummary(),
                plan.getGeneratedAt(),
                sessionResponses
        );
    }

    private StudySessionResponse toSessionResponse(
            StudySession session
    ) {
        StudentCourse course =
                session.getStudentCourse();

        CourseDeadline deadline =
                session.getDeadline();

        StudentGoal goal =
                session.getGoal();

        return new StudySessionResponse(
                session.getId(),
                course != null
                        ? course.getId()
                        : null,
                course != null
                        ? course.getName()
                        : null,
                deadline != null
                        ? deadline.getId()
                        : null,
                deadline != null
                        ? deadline.getTitle()
                        : null,
                goal != null
                        ? goal.getId()
                        : null,
                goal != null
                        ? goal.getTitle()
                        : null,
                session.getTitle(),
                session.getStartAt(),
                session.getEndAt(),
                session.getPlannedMinutes(),
                session.getActualMinutes(),
                session.getStatus(),
                session.getRationale(),
                session.getCompletedAt()
        );
    }

    private String formatMinutes(
            int minutes
    ) {
        int hours =
                minutes / 60;

        int remainder =
                minutes % 60;

        if (hours == 0) {
            return remainder + " min";
        }

        if (remainder == 0) {
            return hours + " hr";
        }

        return hours +
                " hr " +
                remainder +
                " min";
    }

    private void supersedeOtherCurrentPlans(
            Long userId,
            Long currentPlanId
    ) {

        List<PersonalStudyPlan> plans =
                planRepository
                        .findByUserIdOrderByGeneratedAtDesc(
                                userId
                        );

        for (PersonalStudyPlan existingPlan : plans) {

            if (existingPlan.getId().equals(currentPlanId)) {
                continue;
            }

            if ("SUPERSEDED".equalsIgnoreCase(
                    existingPlan.getStatus()
            )) {
                continue;
            }

            existingPlan.setStatus(
                    "SUPERSEDED"
            );

            planRepository.save(
                    existingPlan
            );
        }
    }

    private static class WorkItem {

        private final CourseDeadline deadline;
        private final StudentGoal goal;
        private int remainingMinutes;

        private WorkItem(
                CourseDeadline deadline,
                StudentGoal goal,
                int remainingMinutes
        ) {
            this.deadline =
                    deadline;

            this.goal =
                    goal;

            this.remainingMinutes =
                    remainingMinutes;
        }
    }

    private static class StudyWindow {

        private final LocalDateTime start;
        private final LocalDateTime end;
        private final boolean scheduleBackward;

        private StudyWindow(
                LocalDateTime start,
                LocalDateTime end,
                boolean scheduleBackward
        ) {
            this.start =
                    start;

            this.end =
                    end;

            this.scheduleBackward =
                    scheduleBackward;
        }
    }

    private static class TimeRange {

        private final LocalTime start;
        private final LocalTime end;

        private TimeRange(
                LocalTime start,
                LocalTime end
        ) {
            this.start =
                    start;

            this.end =
                    end;
        }
    }
}
