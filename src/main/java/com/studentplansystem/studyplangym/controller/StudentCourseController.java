package com.studentplansystem.studyplangym.controller;

import com.studentplansystem.studyplangym.dto.StudentCourseRequest;
import com.studentplansystem.studyplangym.dto.StudentCourseResponse;
import com.studentplansystem.studyplangym.service.StudentCourseService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student/courses")
public class StudentCourseController {

    private final StudentCourseService studentCourseService;

    public StudentCourseController(
            StudentCourseService studentCourseService
    ) {
        this.studentCourseService = studentCourseService;
    }

    @PostMapping
    public ResponseEntity<StudentCourseResponse> createCourse(
            @RequestBody StudentCourseRequest request,
            Authentication authentication
    ) {
        StudentCourseResponse created =
                studentCourseService.createCourse(
                        authentication.getName(),
                        request
                );

        return ResponseEntity.ok(created);
    }

    @GetMapping
    public ResponseEntity<List<StudentCourseResponse>> getMyCourses(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                studentCourseService.getCourses(
                        authentication.getName()
                )
        );
    }
@PutMapping("/{courseId}")
public ResponseEntity<StudentCourseResponse> updateCourse(
        @PathVariable Long courseId,
        @RequestBody StudentCourseRequest request,
        Authentication authentication
) {
    return ResponseEntity.ok(
            studentCourseService.updateCourse(
                    authentication.getName(),
                    courseId,
                    request
            )
    );
}

@DeleteMapping("/{courseId}")
public ResponseEntity<Void> deactivateCourse(
        @PathVariable Long courseId,
        Authentication authentication
) {
    studentCourseService.deactivateCourse(
            authentication.getName(),
            courseId
    );

    return ResponseEntity.noContent().build();
}
}
