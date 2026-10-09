package com.studentplansystem.studyplangym.repository;

import com.studentplansystem.studyplangym.entity.PersonalStudyPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PersonalStudyPlanRepository
        extends JpaRepository<PersonalStudyPlan, Long> {

    List<PersonalStudyPlan>
    findByUserIdOrderByGeneratedAtDesc(
            Long userId
    );

    Optional<PersonalStudyPlan>
    findFirstByUserIdOrderByGeneratedAtDesc(
            Long userId
    );

    Optional<PersonalStudyPlan>
    findFirstByUserIdAndStatusOrderByGeneratedAtDesc(
            Long userId,
            String status
    );

    Optional<PersonalStudyPlan>
    findByIdAndUserId(
            Long id,
            Long userId
    );
}
