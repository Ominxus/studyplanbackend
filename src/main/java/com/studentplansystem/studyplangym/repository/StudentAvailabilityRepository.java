package com.studentplansystem.studyplangym.repository;

import com.studentplansystem.studyplangym.entity.StudentAvailability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentAvailabilityRepository
        extends JpaRepository<StudentAvailability, Long> {

    List<StudentAvailability>
    findByUserIdAndActiveTrueOrderByDayOfWeekAscStartTimeAsc(
            Long userId
    );

    Optional<StudentAvailability>
    findByIdAndUserIdAndActiveTrue(
            Long id,
            Long userId
    );
}
