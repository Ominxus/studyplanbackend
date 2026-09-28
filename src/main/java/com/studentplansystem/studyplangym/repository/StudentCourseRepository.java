package com.studentplansystem.studyplangym.repository;

import com.studentplansystem.studyplangym.entity.StudentCourse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentCourseRepository extends JpaRepository<StudentCourse, Long> {

    List<StudentCourse> findByUserId(Long userId);

    List<StudentCourse> findByUserIdAndActiveTrue(Long userId);
}
