package com.studentplansystem.studyplangym.service;

import com.studentplansystem.studyplangym.dto.StudySessionCompletionRequest;
import com.studentplansystem.studyplangym.dto.StudySessionResponse;
import com.studentplansystem.studyplangym.entity.CourseDeadline;
import com.studentplansystem.studyplangym.entity.StudentCourse;
import com.studentplansystem.studyplangym.entity.StudentGoal;
import com.studentplansystem.studyplangym.entity.StudySession;
import com.studentplansystem.studyplangym.entity.User;
import com.studentplansystem.studyplangym.repository.StudySessionRepository;
import com.studentplansystem.studyplangym.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class StudySessionProgressService {

    private final UserRepository userRepository;
    private final StudySessionRepository sessionRepository;

    public StudySessionProgressService(
            UserRepository userRepository,
            StudySessionRepository sessionRepository
    ) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
    }

    @Transactional
    public StudySessionResponse completeSession(
            String username,
            Long sessionId,
            StudySessionCompletionRequest request
    ) {
        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Authenticated user could not be found."
                        )
                );

        StudySession session =
                sessionRepository
                        .findByIdAndPlanUserId(
                                sessionId,
                                user.getId()
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Study session not found."
                                )
                        );

        /*
         * Sessions from an older superseded plan should
         * no longer be completed after adaptive replanning.
         */
        if (
                session.getPlan() != null &&
                "SUPERSEDED".equalsIgnoreCase(
                        session.getPlan().getStatus()
                )
        ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "This session belongs to a superseded study plan."
            );
        }

        if (
                "COMPLETED".equalsIgnoreCase(
                        session.getStatus()
                )
        ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "This study session is already completed."
            );
        }

        Integer actualMinutes =
                request != null
                        ? request.getActualMinutes()
                        : null;

        if (actualMinutes == null) {
            actualMinutes =
                    session.getPlannedMinutes();
        }

        if (actualMinutes <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Actual study time must be greater than zero."
            );
        }

        if (actualMinutes > 1440) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Actual study time cannot exceed 24 hours."
            );
        }

        session.setActualMinutes(
                actualMinutes
        );

        session.setStatus(
                "COMPLETED"
        );

        session.setCompletedAt(
                LocalDateTime.now()
        );

        session =
                sessionRepository.save(
                        session
                );

        return toResponse(
                session
        );
    }

    private StudySessionResponse toResponse(
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
}
