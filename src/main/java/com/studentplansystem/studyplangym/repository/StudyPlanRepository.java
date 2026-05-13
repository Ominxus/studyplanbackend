package com.studentplansystem.studyplangym.repository;

import com.studentplansystem.studyplangym.entity.StudyPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudyPlanRepository extends JpaRepository<StudyPlan, Long> {

    boolean existsByStudentNumber(String studentNumber);

    Optional<StudyPlan> findByStudentNumber(String studentNumber);

    List<StudyPlan> findByEditRequestStatus(String editRequestStatus);
}