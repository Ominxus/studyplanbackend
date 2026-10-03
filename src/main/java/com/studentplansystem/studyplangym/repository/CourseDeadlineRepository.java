package com.studentplansystem.studyplangym.repository;

import com.studentplansystem.studyplangym.entity.CourseDeadline;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseDeadlineRepository
        extends JpaRepository<CourseDeadline, Long> {

    List<CourseDeadline>
    findByStudentCourseUserIdAndStatusNotOrderByDueAtAsc(
            Long userId,
            String status
    );

    List<CourseDeadline>
    findByStudentCourseIdAndStudentCourseUserIdAndStatusNotOrderByDueAtAsc(
            Long courseId,
            Long userId,
            String status
    );

    List<CourseDeadline>
    findByStudentCourseUserIdAndStatusOrderByDueAtAsc(
            Long userId,
            String status
    );

    Optional<CourseDeadline>
    findByIdAndStudentCourseUserId(
            Long id,
            Long userId
    );
}
