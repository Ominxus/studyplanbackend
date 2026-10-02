package com.studentplansystem.studyplangym.repository;

import com.studentplansystem.studyplangym.entity.StudentPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentPreferenceRepository
        extends JpaRepository<StudentPreference, Long> {

    Optional<StudentPreference> findByUserId(
            Long userId
    );
}
