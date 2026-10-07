package com.studentplansystem.studyplangym.controller;

import com.studentplansystem.studyplangym.dto.StudySessionCompletionRequest;
import com.studentplansystem.studyplangym.dto.StudySessionResponse;
import com.studentplansystem.studyplangym.service.StudySessionProgressService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student/sessions")
public class StudySessionController {

    private final StudySessionProgressService progressService;

    public StudySessionController(
            StudySessionProgressService progressService
    ) {
        this.progressService = progressService;
    }

    @PatchMapping("/{sessionId}/complete")
    public ResponseEntity<StudySessionResponse> completeSession(
            @PathVariable Long sessionId,
            @RequestBody(required = false)
            StudySessionCompletionRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                progressService.completeSession(
                        authentication.getName(),
                        sessionId,
                        request
                )
        );
    }
}
