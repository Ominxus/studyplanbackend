package com.studentplansystem.studyplangym.service;

import com.studentplansystem.studyplangym.entity.CourseDeadline;
import com.studentplansystem.studyplangym.entity.PersonalStudyPlan;
import com.studentplansystem.studyplangym.entity.StudentAvailability;
import com.studentplansystem.studyplangym.entity.StudentCourse;
import com.studentplansystem.studyplangym.entity.StudentGoal;
import com.studentplansystem.studyplangym.entity.StudentPreference;
import com.studentplansystem.studyplangym.entity.StudySession;
import com.studentplansystem.studyplangym.entity.User;
import com.studentplansystem.studyplangym.repository.CourseDeadlineRepository;
import com.studentplansystem.studyplangym.repository.PersonalStudyPlanRepository;
import com.studentplansystem.studyplangym.repository.StudentAvailabilityRepository;
import com.studentplansystem.studyplangym.repository.StudentCourseRepository;
import com.studentplansystem.studyplangym.repository.StudentGoalRepository;
import com.studentplansystem.studyplangym.repository.StudentPreferenceRepository;
import com.studentplansystem.studyplangym.repository.StudySessionRepository;
import com.studentplansystem.studyplangym.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class AiPlanningContextService {

    private final UserRepository userRepository;
    private final StudentCourseRepository studentCourseRepository;
    private final CourseDeadlineRepository courseDeadlineRepository;
    private final StudentAvailabilityRepository studentAvailabilityRepository;
    private final StudentPreferenceRepository studentPreferenceRepository;
    private final StudentGoalRepository studentGoalRepository;
    private final PersonalStudyPlanRepository personalStudyPlanRepository;
    private final StudySessionRepository studySessionRepository;

    public AiPlanningContextService(
            UserRepository userRepository,
            StudentCourseRepository studentCourseRepository,
            CourseDeadlineRepository courseDeadlineRepository,
            StudentAvailabilityRepository studentAvailabilityRepository,
            StudentPreferenceRepository studentPreferenceRepository,
            StudentGoalRepository studentGoalRepository,
            PersonalStudyPlanRepository personalStudyPlanRepository,
            StudySessionRepository studySessionRepository
    ) {
        this.userRepository = userRepository;
        this.studentCourseRepository = studentCourseRepository;
        this.courseDeadlineRepository = courseDeadlineRepository;
        this.studentAvailabilityRepository = studentAvailabilityRepository;
        this.studentPreferenceRepository = studentPreferenceRepository;
        this.studentGoalRepository = studentGoalRepository;
        this.personalStudyPlanRepository = personalStudyPlanRepository;
        this.studySessionRepository = studySessionRepository;
    }

    @Transactional(readOnly = true)
    public String buildContext(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Student account not found."
                ));

        Long userId = user.getId();

        List<StudentCourse> courses =
                studentCourseRepository.findByUserIdAndActiveTrue(userId);

        List<CourseDeadline> deadlines =
                courseDeadlineRepository
                        .findByStudentCourseUserIdAndStatusOrderByDueAtAsc(
                                userId,
                                "PENDING"
                        );

        List<StudentAvailability> availability =
                studentAvailabilityRepository
                        .findByUserIdAndActiveTrueOrderByDayOfWeekAscStartTimeAsc(
                                userId
                        );

        Optional<StudentPreference> preference =
                studentPreferenceRepository.findByUserId(userId);

        List<StudentGoal> goals =
                studentGoalRepository
                        .findByUserIdAndStatusOrderByTargetDateAsc(
                                userId,
                                "ACTIVE"
                        );

        Optional<PersonalStudyPlan> latestPlan =
                personalStudyPlanRepository
                        .findFirstByUserIdAndStatusOrderByGeneratedAtDesc(
                                userId,
                                "GENERATED"
                        );

        StringBuilder context = new StringBuilder();

        context.append("CURRENT DATE\n");
        context.append(LocalDate.now()).append("\n\n");

        context.append("ACTIVE COURSES\n");

        if (courses.isEmpty()) {
            context.append("None.\n");
        } else {
            for (StudentCourse course : courses) {
                context.append("- ")
                        .append(course.getName())
                        .append(" | code=")
                        .append(course.getCode())
                        .append(" | difficulty=")
                        .append(course.getDifficulty())
                        .append("/5 | priority=")
                        .append(course.getPriority())
                        .append("/5\n");
            }
        }

        context.append("\nPENDING DEADLINES\n");

        if (deadlines.isEmpty()) {
            context.append("None.\n");
        } else {
            for (CourseDeadline deadline : deadlines) {

                int estimatedMinutes =
                        deadline.getEstimatedMinutes() != null
                                ? deadline.getEstimatedMinutes()
                                : 0;

                int completedMinutes =
                        studySessionRepository
                                .findByDeadlineIdAndPlanUserIdAndStatus(
                                        deadline.getId(),
                                        userId,
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

                context.append("- ")
                        .append(deadline.getTitle())
                        .append(" | course=")
                        .append(deadline.getStudentCourse().getName())
                        .append(" | type=")
                        .append(deadline.getDeadlineType())
                        .append(" | due=")
                        .append(deadline.getDueAt())
                        .append(" | importance=")
                        .append(deadline.getImportance())
                        .append("/5 | estimatedMinutes=")
                        .append(estimatedMinutes)
                        .append(" | completedStudyMinutes=")
                        .append(completedMinutes)
                        .append(" | remainingEstimatedMinutes=")
                        .append(remainingMinutes)
                        .append("\n");
            }
        }

        context.append("\nACTIVE GOALS\n");

        if (goals.isEmpty()) {
            context.append("None.\n");
        } else {
            for (StudentGoal goal : goals) {
                context.append("- ")
                        .append(goal.getTitle())
                        .append(" | targetDate=")
                        .append(goal.getTargetDate())
                        .append(" | priority=")
                        .append(goal.getPriority())
                        .append("/5");

                if (goal.getDescription() != null
                        && !goal.getDescription().isBlank()) {
                    context.append(" | description=")
                            .append(goal.getDescription());
                }

                context.append("\n");
            }
        }

        context.append("\nWEEKLY AVAILABILITY\n");

        if (availability.isEmpty()) {
            context.append("None.\n");
        } else {
            for (StudentAvailability slot : availability) {

                String day;

                try {
                    day = DayOfWeek.of(slot.getDayOfWeek()).name();
                } catch (Exception exception) {
                    day = "DAY_" + slot.getDayOfWeek();
                }

                context.append("- ")
                        .append(day)
                        .append(" ")
                        .append(slot.getStartTime())
                        .append(" - ")
                        .append(slot.getEndTime())
                        .append("\n");
            }
        }

        context.append("\nSTUDY PREFERENCES\n");

        if (preference.isPresent()) {

            StudentPreference pref =
                    preference.get();

            context.append("- preferredSessionMinutes=")
                    .append(pref.getPreferredSessionMinutes())
                    .append("\n");

            context.append("- maximumSessionMinutes=")
                    .append(pref.getMaximumSessionMinutes())
                    .append("\n");

            context.append("- breakMinutes=")
                    .append(pref.getBreakMinutes())
                    .append("\n");

            context.append("- preferredStudyPeriod=")
                    .append(pref.getPreferredStudyPeriod())
                    .append("\n");

            context.append("- maximumDailyMinutes=")
                    .append(pref.getMaximumDailyMinutes())
                    .append("\n");

        } else {
            context.append("No custom preferences saved.\n");
        }

        context.append("\nLATEST GENERATED STUDY PLAN\n");

        if (latestPlan.isEmpty()) {
            context.append("No generated study plan exists.\n");
        } else {

            PersonalStudyPlan plan =
                    latestPlan.get();

            context.append("- planId=")
                    .append(plan.getId())
                    .append("\n");

            context.append("- name=")
                    .append(plan.getPlanName())
                    .append("\n");

            context.append("- generationMethod=")
                    .append(plan.getGenerationMethod())
                    .append("\n");

            context.append("- status=")
                    .append(plan.getStatus())
                    .append("\n");

            context.append("- period=")
                    .append(plan.getStartDate())
                    .append(" to ")
                    .append(plan.getEndDate())
                    .append("\n");

            if (plan.getSourcePlan() != null) {
                context.append("- adaptedFromPlanId=")
                        .append(plan.getSourcePlan().getId())
                        .append("\n");
            }

            List<StudySession> sessions =
                    studySessionRepository
                            .findByPlanIdOrderByStartAtAsc(
                                    plan.getId()
                            );

            context.append("\nCURRENT PLAN SESSIONS\n");

            if (sessions.isEmpty()) {
                context.append("None.\n");
            } else {
                for (StudySession session : sessions) {

                    context.append("- ")
                            .append(session.getTitle())
                            .append(" | start=")
                            .append(session.getStartAt())
                            .append(" | end=")
                            .append(session.getEndAt())
                            .append(" | plannedMinutes=")
                            .append(session.getPlannedMinutes())
                            .append(" | status=")
                            .append(session.getStatus());

                    if (session.getActualMinutes() != null) {
                        context.append(" | actualMinutes=")
                                .append(session.getActualMinutes());
                    }

                    if (session.getStudentCourse() != null) {
                        context.append(" | course=")
                                .append(
                                        session
                                                .getStudentCourse()
                                                .getName()
                                );
                    }

                    context.append("\n");
                }
            }
        }

        return context.toString();
    }
}
