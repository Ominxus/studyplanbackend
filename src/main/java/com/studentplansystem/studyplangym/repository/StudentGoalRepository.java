package com.studentplansystem.studyplangym.repository;

import com.studentplansystem.studyplangym.entity.StudentGoal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentGoalRepository
        extends JpaRepository<StudentGoal, Long> {

    List<StudentGoal>
    findByUserIdAndStatusNotOrderByTargetDateAsc(
            Long userId,
            String status
    );

    Optional<StudentGoal>
    findByIdAndUserId(
            Long id,
            Long userId
    );
}
