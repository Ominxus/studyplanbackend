package com.studentplansystem.studyplangym.controller;

import com.studentplansystem.studyplangym.dto.StudentAvailabilityRequest;
import com.studentplansystem.studyplangym.dto.StudentAvailabilityResponse;
import com.studentplansystem.studyplangym.service.StudentAvailabilityService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student/availability")
public class StudentAvailabilityController {

    private final StudentAvailabilityService availabilityService;

    public StudentAvailabilityController(
            StudentAvailabilityService availabilityService
    ) {
        this.availabilityService = availabilityService;
    }

    @PostMapping
    public ResponseEntity<StudentAvailabilityResponse> createAvailability(
            @RequestBody StudentAvailabilityRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                availabilityService.createAvailability(
                        authentication.getName(),
                        request
                )
        );
    }

    @GetMapping
    public ResponseEntity<List<StudentAvailabilityResponse>> getAvailability(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                availabilityService.getAvailability(
                        authentication.getName()
                )
        );
    }

    @PutMapping("/{availabilityId}")
    public ResponseEntity<StudentAvailabilityResponse> updateAvailability(
            @PathVariable Long availabilityId,
            @RequestBody StudentAvailabilityRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                availabilityService.updateAvailability(
                        authentication.getName(),
                        availabilityId,
                        request
                )
        );
    }

    @DeleteMapping("/{availabilityId}")
    public ResponseEntity<Void> deactivateAvailability(
            @PathVariable Long availabilityId,
            Authentication authentication
    ) {
        availabilityService.deactivateAvailability(
                authentication.getName(),
                availabilityId
        );

        return ResponseEntity.noContent().build();
    }
}
