package com.studentplansystem.studyplangym.controller;

import com.studentplansystem.studyplangym.dto.StudentGoalRequest;
import com.studentplansystem.studyplangym.dto.StudentGoalResponse;
import com.studentplansystem.studyplangym.service.StudentGoalService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student/goals")
public class StudentGoalController {

    private final StudentGoalService goalService;

    public StudentGoalController(
            StudentGoalService goalService
    ) {
        this.goalService = goalService;
    }

    @PostMapping
    public ResponseEntity<StudentGoalResponse> createGoal(
            @RequestBody StudentGoalRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                goalService.createGoal(
                        authentication.getName(),
                        request
                )
        );
    }

    @GetMapping
    public ResponseEntity<List<StudentGoalResponse>> getGoals(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                goalService.getGoals(
                        authentication.getName()
                )
        );
    }

    @PutMapping("/{goalId}")
    public ResponseEntity<StudentGoalResponse> updateGoal(
            @PathVariable Long goalId,
            @RequestBody StudentGoalRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                goalService.updateGoal(
                        authentication.getName(),
                        goalId,
                        request
                )
        );
    }

    @PatchMapping("/{goalId}/complete")
    public ResponseEntity<StudentGoalResponse> completeGoal(
            @PathVariable Long goalId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                goalService.completeGoal(
                        authentication.getName(),
                        goalId
                )
        );
    }

    @DeleteMapping("/{goalId}")
    public ResponseEntity<Void> cancelGoal(
            @PathVariable Long goalId,
            Authentication authentication
    ) {
        goalService.cancelGoal(
                authentication.getName(),
                goalId
        );

        return ResponseEntity.noContent().build();
    }
}
