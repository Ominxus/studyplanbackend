package com.studentplansystem.studyplangym.controller;

import com.studentplansystem.studyplangym.dto.StudentPreferenceRequest;
import com.studentplansystem.studyplangym.dto.StudentPreferenceResponse;
import com.studentplansystem.studyplangym.service.StudentPreferenceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student/preferences")
public class StudentPreferenceController {

    private final StudentPreferenceService preferenceService;

    public StudentPreferenceController(
            StudentPreferenceService preferenceService
    ) {
        this.preferenceService =
                preferenceService;
    }

    @GetMapping
    public ResponseEntity<StudentPreferenceResponse> getPreferences(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                preferenceService.getPreferences(
                        authentication.getName()
                )
        );
    }

    @PutMapping
    public ResponseEntity<StudentPreferenceResponse> updatePreferences(
            @RequestBody StudentPreferenceRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                preferenceService.updatePreferences(
                        authentication.getName(),
                        request
                )
        );
    }
}
