package com.studentplansystem.studyplangym.controller;

import com.studentplansystem.studyplangym.entity.Category;
import com.studentplansystem.studyplangym.entity.StudyPlan;
import com.studentplansystem.studyplangym.entity.SubjectEntry;
import com.studentplansystem.studyplangym.entity.SubjectOption;
import com.studentplansystem.studyplangym.repository.CategoryRepository;
import com.studentplansystem.studyplangym.repository.StudyPlanRepository;
import com.studentplansystem.studyplangym.service.AuditLogService;
import com.studentplansystem.studyplangym.util.ExcelExporter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/studyplans")
public class StudyPlanController {

    private final StudyPlanRepository repository;
    private final CategoryRepository categoryRepository;
    private final AuditLogService auditLogService;

    public StudyPlanController(
            StudyPlanRepository repository,
            CategoryRepository categoryRepository,
            AuditLogService auditLogService
    ) {
        this.repository = repository;
        this.categoryRepository = categoryRepository;
        this.auditLogService = auditLogService;
    }

    @PostMapping
    public ResponseEntity<?> createStudyPlan(
            @RequestBody StudyPlan studyPlan,
            Authentication authentication
    ) {
        String loggedInUsername = authentication.getName();

        if (studyPlan.getStudentNumber() == null || studyPlan.getStudentNumber().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Student number is required.");
        }

        String studentNumber = studyPlan.getStudentNumber().trim();

        if (!studentNumber.equals(loggedInUsername)) {
            return ResponseEntity.status(403).body("You can only submit a study plan for your own student number.");
        }

        if (repository.existsByStudentNumber(studentNumber)) {
            return ResponseEntity
                    .badRequest()
                    .body("You have already submitted a study plan. Only one submission is allowed.");
        }

        List<String> validationErrors = validateStudyPlan(studyPlan);

        if (!validationErrors.isEmpty()) {
            return ResponseEntity.badRequest().body(String.join("\n", validationErrors));
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
    public ResponseEntity<?> getStudyPlanByStudentNumber(
            @PathVariable String studentNumber,
            Authentication authentication
    ) {
        String requestedStudentNumber = studentNumber.trim();

        if (isStudent(authentication) && !requestedStudentNumber.equals(authentication.getName())) {
            return ResponseEntity.status(403).body("You can only view your own study plan.");
        }

        return repository.findByStudentNumber(requestedStudentNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/edit-requests")
    public List<StudyPlan> getPendingEditRequests() {
        return repository.findByEditRequestStatus("PENDING");
    }

    @PostMapping("/request-edit/{studentNumber}")
    public ResponseEntity<?> requestEditPermission(
            @PathVariable String studentNumber,
            Authentication authentication
    ) {
        String requestedStudentNumber = studentNumber.trim();

        if (!requestedStudentNumber.equals(authentication.getName())) {
            return ResponseEntity.status(403).body("You can only request editing for your own study plan.");
        }

        return repository.findByStudentNumber(requestedStudentNumber)
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
    public ResponseEntity<?> approveEditRequest(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return repository.findById(id)
                .map(plan -> {
                    plan.setEditRequested(false);
                    plan.setEditApproved(true);
                    plan.setEditRequestStatus("APPROVED");

                    repository.save(plan);

                    auditLogService.log(
                            authentication,
                            "APPROVE_EDIT_REQUEST",
                            "Approved edit request for student: " + plan.getStudentNumber()
                    );

                    return ResponseEntity.ok("Edit request approved.");
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/deny-edit/{id}")
    public ResponseEntity<?> denyEditRequest(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return repository.findById(id)
                .map(plan -> {
                    plan.setEditRequested(false);
                    plan.setEditApproved(false);
                    plan.setEditRequestStatus("DENIED");

                    repository.save(plan);

                    auditLogService.log(
                            authentication,
                            "DENY_EDIT_REQUEST",
                            "Denied edit request for student: " + plan.getStudentNumber()
                    );

                    return ResponseEntity.ok("Edit request denied.");
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/update/{studentNumber}")
    public ResponseEntity<?> updateStudyPlan(
            @PathVariable String studentNumber,
            @RequestBody StudyPlan updatedStudyPlan,
            Authentication authentication
    ) {
        String requestedStudentNumber = studentNumber.trim();

        if (!requestedStudentNumber.equals(authentication.getName())) {
            return ResponseEntity.status(403).body("You can only update your own study plan.");
        }

        List<String> validationErrors = validateStudyPlan(updatedStudyPlan);

        if (!validationErrors.isEmpty()) {
            return ResponseEntity.badRequest().body(String.join("\n", validationErrors));
        }

        return repository.findByStudentNumber(requestedStudentNumber)
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
    public ResponseEntity<?> deleteStudyPlan(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Optional<StudyPlan> plan = repository.findById(id);

        if (plan.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        String studentNumber = plan.get().getStudentNumber();
        String fullName = plan.get().getFullName();

        repository.deleteById(id);

        auditLogService.log(
                authentication,
                "DELETE_STUDY_PLAN",
                "Deleted study plan for " + fullName + " (" + studentNumber + ")"
        );

        return ResponseEntity.ok().build();
    }

    @GetMapping("/export")
    public void export(
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        response.setContentType("application/octet-stream");
        response.setHeader(
                "Content-Disposition",
                "attachment; filename=studyplans.xlsx"
        );

        auditLogService.log(
                authentication,
                "EXPORT_EXCEL",
                "Exported study plans to Excel"
        );

        ExcelExporter.exportNewFormat(repository.findAll(), response);
    }

    private List<String> validateStudyPlan(StudyPlan studyPlan) {
        List<String> errors = new ArrayList<>();

        if (studyPlan.getFullName() == null || studyPlan.getFullName().trim().isEmpty()) {
            errors.add("Full name is required.");
        }

        if (studyPlan.getClassYear() == null || studyPlan.getClassYear().trim().isEmpty()) {
            errors.add("Class is required.");
        }

        if (studyPlan.getSchoolYear() == null || studyPlan.getSchoolYear().trim().isEmpty()) {
            errors.add("School years are required.");
        }

        if (studyPlan.getSubjects() == null || studyPlan.getSubjects().isEmpty()) {
            errors.add("At least one subject must be selected.");
            return errors;
        }

        List<Category> categories = categoryRepository.findAll();

        Map<String, SubjectOption> allowedSubjectsByName = new HashMap<>();
        Map<String, String> subjectCategoryByName = new HashMap<>();
        Map<String, String> subjectGroupByName = new HashMap<>();

        for (Category category : categories) {
            if (category.getSubjects() == null) {
                continue;
            }

            for (SubjectOption option : category.getSubjects()) {
                allowedSubjectsByName.put(option.getName(), option);
                subjectCategoryByName.put(option.getName(), category.getName());

                String groupName = normalizeGroupName(option);

                if (groupName != null) {
                    subjectGroupByName.put(option.getName(), groupName);
                }
            }
        }

        Set<String> selectedNames = new HashSet<>();
        Map<String, Integer> selectedCountByCategory = new HashMap<>();
        Map<String, String> selectedSubjectByGroup = new HashMap<>();

        for (SubjectEntry entry : studyPlan.getSubjects()) {
            if (entry.getSubject() == null || entry.getSubject().trim().isEmpty()) {
                errors.add("Selected subject name cannot be empty.");
                continue;
            }

            String subjectName = entry.getSubject().trim();

            if (selectedNames.contains(subjectName)) {
                errors.add("Duplicate subject selected: " + subjectName);
                continue;
            }

            selectedNames.add(subjectName);

            SubjectOption allowedSubject = allowedSubjectsByName.get(subjectName);

            if (allowedSubject == null) {
                errors.add("Invalid subject selected: " + subjectName);
                continue;
            }

            if (entry.getGradeIiiHours() != allowedSubject.getGradeIiiHours()) {
                errors.add(subjectName + " has invalid Grade III hours.");
            }

            if (entry.getGradeIvHours() != allowedSubject.getGradeIvHours()) {
                errors.add(subjectName + " has invalid Grade IV hours.");
            }

            String categoryName = subjectCategoryByName.get(subjectName);

            selectedCountByCategory.put(
                    categoryName,
                    selectedCountByCategory.getOrDefault(categoryName, 0) + 1
            );

            String groupName = subjectGroupByName.get(subjectName);

            if (groupName != null) {
                if (selectedSubjectByGroup.containsKey(groupName)) {
                    errors.add(
                            "Only one subject can be selected from group '" +
                                    groupName +
                                    "': " +
                                    selectedSubjectByGroup.get(groupName) +
                                    " and " +
                                    subjectName
                    );
                } else {
                    selectedSubjectByGroup.put(groupName, subjectName);
                }
            }
        }

        for (Category category : categories) {
            int selectedCount = selectedCountByCategory.getOrDefault(category.getName(), 0);

            if (!category.isOptional() && selectedCount < category.getMinRequired()) {
                errors.add(
                        category.getName() +
                                ": select at least " +
                                category.getMinRequired() +
                                " subject(s)."
                );
            }

            if (selectedCount > category.getMaxAllowed()) {
                errors.add(
                        category.getName() +
                                ": select no more than " +
                                category.getMaxAllowed() +
                                " subject(s)."
                );
            }
        }

        return errors;
    }

    private String normalizeGroupName(SubjectOption option) {
        if (option.getGroupName() != null && !option.getGroupName().trim().isEmpty()) {
            return option.getGroupName().trim().toLowerCase();
        }

        String subjectName = option.getName() == null
                ? ""
                : option.getName().toLowerCase();

        if (subjectName.contains("matematika")) {
            return "matematika";
        }

        if (
                subjectName.contains("lietuvių kalba") ||
                        subjectName.contains("lietuviu kalba")
        ) {
            return "lietuviu";
        }

        return null;
    }

    private boolean isStudent(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_STUDENT"));
    }
}