package com.studentplansystem.studyplangym.controller;

import com.studentplansystem.studyplangym.dto.CourseDeadlineRequest;
import com.studentplansystem.studyplangym.dto.CourseDeadlineResponse;
import com.studentplansystem.studyplangym.service.CourseDeadlineService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student/deadlines")
public class CourseDeadlineController {

    private final CourseDeadlineService courseDeadlineService;

    public CourseDeadlineController(
            CourseDeadlineService courseDeadlineService
    ) {
        this.courseDeadlineService = courseDeadlineService;
    }

    @PostMapping
    public ResponseEntity<CourseDeadlineResponse> createDeadline(
            @RequestBody CourseDeadlineRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                courseDeadlineService.createDeadline(
                        authentication.getName(),
                        request
                )
        );
    }

    @GetMapping
    public ResponseEntity<List<CourseDeadlineResponse>> getDeadlines(
            @RequestParam(required = false) Long courseId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                courseDeadlineService.getDeadlines(
                        authentication.getName(),
                        courseId
                )
        );
    }
}
