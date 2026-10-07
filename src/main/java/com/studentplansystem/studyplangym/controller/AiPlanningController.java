package com.studentplansystem.studyplangym.controller;

import com.studentplansystem.studyplangym.dto.AiPlanningInsightResponse;
import com.studentplansystem.studyplangym.service.AiPlanningService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/student/ai")
public class AiPlanningController {

    private final AiPlanningService aiPlanningService;

    public AiPlanningController(
            AiPlanningService aiPlanningService
    ) {
        this.aiPlanningService =
                aiPlanningService;
    }

    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testAi() {

        String result =
                aiPlanningService.testConnection();

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put(
                "status",
                "connected"
        );

        response.put(
                "model",
                aiPlanningService.getModel()
        );

        response.put(
                "message",
                result
        );

        return ResponseEntity.ok(
                response
        );
    }

    @GetMapping("/planning-insight")
    public ResponseEntity<AiPlanningInsightResponse>
    getPlanningInsight(
            Authentication authentication
    ) {

        AiPlanningInsightResponse insight =
                aiPlanningService
                        .getPlanningInsight(
                                authentication.getName()
                        );

        return ResponseEntity.ok(
                insight
        );
    }
}
