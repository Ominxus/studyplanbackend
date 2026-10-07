package com.studentplansystem.studyplangym.controller;

import com.studentplansystem.studyplangym.dto.PersonalStudyPlanResponse;
import com.studentplansystem.studyplangym.dto.StudyPlanGenerationRequest;
import com.studentplansystem.studyplangym.service.StudyPlanGeneratorService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student/plans")
public class PersonalStudyPlanController {

    private final StudyPlanGeneratorService generatorService;

    public PersonalStudyPlanController(
            StudyPlanGeneratorService generatorService
    ) {
        this.generatorService = generatorService;
    }

    @PostMapping("/generate")
    public ResponseEntity<PersonalStudyPlanResponse> generatePlan(
            @RequestBody StudyPlanGenerationRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                generatorService.generatePlan(
                        authentication.getName(),
                        request
                )
        );
    }

    @PostMapping("/{planId}/replan")
    public ResponseEntity<PersonalStudyPlanResponse> replan(
            @PathVariable Long planId,
            @RequestBody StudyPlanGenerationRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                generatorService.replan(
                        authentication.getName(),
                        planId,
                        request
                )
        );
    }

    @GetMapping("/latest")
    public ResponseEntity<PersonalStudyPlanResponse> getLatestPlan(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                generatorService.getLatestPlan(
                        authentication.getName()
                )
        );
    }
}
