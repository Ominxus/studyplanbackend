package com.studentplansystem.studyplangym.controller;

import com.studentplansystem.studyplangym.entity.StudyPlan;
import com.studentplansystem.studyplangym.repository.StudyPlanRepository;
import com.studentplansystem.studyplangym.util.ExcelExporter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/studyplans")
public class StudyPlanController {

    private final StudyPlanRepository repository;

    public StudyPlanController(StudyPlanRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public ResponseEntity<?> createStudyPlan(@RequestBody StudyPlan studyPlan) {
        if (studyPlan.getStudentNumber() == null || studyPlan.getStudentNumber().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Student number is required.");
        }

        String studentNumber = studyPlan.getStudentNumber().trim();

        if (repository.existsByStudentNumber(studentNumber)) {
            return ResponseEntity
                    .badRequest()
                    .body("You have already submitted a study plan. Only one submission is allowed.");
        }

        studyPlan.setStudentNumber(studentNumber);
        studyPlan.setEditRequested(false);
        studyPlan.setEditApproved(false);
        studyPlan.setEditRequestStatus("NONE");

        StudyPlan savedPlan = repository.save(studyPlan);
        return ResponseEntity.ok(savedPlan);
    }

    @GetMapping
    public List<StudyPlan> getAllStudyPlans() {
        return repository.findAll();
    }

    @GetMapping("/student/{studentNumber}")
    public ResponseEntity<?> getStudyPlanByStudentNumber(@PathVariable String studentNumber) {
        return repository.findByStudentNumber(studentNumber.trim())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/edit-requests")
    public List<StudyPlan> getPendingEditRequests() {
        return repository.findByEditRequestStatus("PENDING");
    }

    @PostMapping("/request-edit/{studentNumber}")
    public ResponseEntity<?> requestEditPermission(@PathVariable String studentNumber) {
        return repository.findByStudentNumber(studentNumber.trim())
                .map(plan -> {
                    if (plan.isEditApproved()) {
                        return ResponseEntity.badRequest().body("Edit permission is already approved.");
                    }

                    if ("PENDING".equalsIgnoreCase(plan.getEditRequestStatus())) {
                        return ResponseEntity.badRequest().body("Edit request is already pending.");
                    }

                    plan.setEditRequested(true);
                    plan.setEditApproved(false);
                    plan.setEditRequestStatus("PENDING");

                    repository.save(plan);
                    return ResponseEntity.ok("Edit request sent to admin.");
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/approve-edit/{id}")
    public ResponseEntity<?> approveEditRequest(@PathVariable Long id) {
        return repository.findById(id)
                .map(plan -> {
                    plan.setEditRequested(false);
                    plan.setEditApproved(true);
                    plan.setEditRequestStatus("APPROVED");

                    repository.save(plan);
                    return ResponseEntity.ok("Edit request approved.");
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/deny-edit/{id}")
    public ResponseEntity<?> denyEditRequest(@PathVariable Long id) {
        return repository.findById(id)
                .map(plan -> {
                    plan.setEditRequested(false);
                    plan.setEditApproved(false);
                    plan.setEditRequestStatus("DENIED");

                    repository.save(plan);
                    return ResponseEntity.ok("Edit request denied.");
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/update/{studentNumber}")
    public ResponseEntity<?> updateStudyPlan(
            @PathVariable String studentNumber,
            @RequestBody StudyPlan updatedStudyPlan
    ) {
        return repository.findByStudentNumber(studentNumber.trim())
                .map(existingPlan -> {
                    if (!existingPlan.isEditApproved()) {
                        return ResponseEntity
                                .badRequest()
                                .body("You are not allowed to edit this study plan yet. Please request admin permission.");
                    }

                    existingPlan.setFullName(updatedStudyPlan.getFullName());
                    existingPlan.setClassYear(updatedStudyPlan.getClassYear());
                    existingPlan.setSchoolYear(updatedStudyPlan.getSchoolYear());

                    if (existingPlan.getSubjects() != null) {
                        existingPlan.getSubjects().clear();

                        if (updatedStudyPlan.getSubjects() != null) {
                            existingPlan.getSubjects().addAll(updatedStudyPlan.getSubjects());
                        }
                    } else {
                        existingPlan.setSubjects(updatedStudyPlan.getSubjects());
                    }

                    existingPlan.setEditRequested(false);
                    existingPlan.setEditApproved(false);
                    existingPlan.setEditRequestStatus("NONE");

                    StudyPlan savedPlan = repository.save(existingPlan);
                    return ResponseEntity.ok(savedPlan);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStudyPlan(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        repository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/export")
    public void export(HttpServletResponse response) throws IOException {
        response.setContentType("application/octet-stream");
        response.setHeader(
                "Content-Disposition",
                "attachment; filename=studyplans.xlsx"
        );

        ExcelExporter.exportNewFormat(repository.findAll(), response);
    }
}