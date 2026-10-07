package com.studentplansystem.studyplangym.repository;

import com.studentplansystem.studyplangym.entity.StudySession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudySessionRepository
        extends JpaRepository<StudySession, Long> {

    List<StudySession>
    findByPlanIdOrderByStartAtAsc(
            Long planId
    );

    Optional<StudySession>
    findByIdAndPlanUserId(
            Long sessionId,
            Long userId
    );

    List<StudySession>
    findByDeadlineIdAndPlanUserIdAndStatus(
            Long deadlineId,
            Long userId,
            String status
    );
}
