package com.studentplansystem.studyplangym.repository;

import com.studentplansystem.studyplangym.entity.StudyPlan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyPlanRepository extends JpaRepository<StudyPlan, Long> {

    boolean existsByStudentNumber(String studentNumber);
}