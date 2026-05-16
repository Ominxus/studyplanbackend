package com.studentplansystem.studyplangym.controller;

import com.studentplansystem.studyplangym.entity.Category;
import com.studentplansystem.studyplangym.entity.SubjectOption;
import com.studentplansystem.studyplangym.entity.SchoolYearOption;
import com.studentplansystem.studyplangym.repository.CategoryRepository;
import com.studentplansystem.studyplangym.repository.SubjectOptionRepository;
import com.studentplansystem.studyplangym.repository.SchoolYearOptionRepository;
import com.studentplansystem.studyplangym.service.AuditLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/config")
public class StudyPlanConfigController {

    private final CategoryRepository categoryRepository;
    private final SubjectOptionRepository subjectOptionRepository;
    private final SchoolYearOptionRepository schoolYearOptionRepository;
    private final AuditLogService auditLogService;

    public StudyPlanConfigController(
            CategoryRepository categoryRepository,
            SubjectOptionRepository subjectOptionRepository,
            SchoolYearOptionRepository schoolYearOptionRepository,
            AuditLogService auditLogService
    ) {
        this.categoryRepository = categoryRepository;
        this.subjectOptionRepository = subjectOptionRepository;
        this.schoolYearOptionRepository = schoolYearOptionRepository;
        this.auditLogService = auditLogService;
    }

    @GetMapping("/categories")
    public List<Category> getCategories() {
        return categoryRepository.findAll();
    }

    @PostMapping("/categories")
    public Category createCategory(
            @RequestBody Category category,
            Authentication authentication
    ) {
        Category savedCategory = categoryRepository.save(category);

        auditLogService.log(
                authentication,
                "CREATE_CATEGORY",
                "Created category: " + savedCategory.getName()
        );

        return savedCategory;
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<Category> updateCategory(
            @PathVariable Long id,
            @RequestBody Category updatedCategory,
            Authentication authentication
    ) {
        return categoryRepository.findById(id)
                .map(category -> {
                    String oldName = category.getName();

                    category.setName(updatedCategory.getName());
                    category.setMinRequired(updatedCategory.getMinRequired());
                    category.setMaxAllowed(updatedCategory.getMaxAllowed());
                    category.setOptional(updatedCategory.isOptional());

                    Category savedCategory = categoryRepository.save(category);

                    auditLogService.log(
                            authentication,
                            "UPDATE_CATEGORY",
                            "Updated category ID " + id + " from '" + oldName + "' to '" + savedCategory.getName() + "'"
                    );

                    return ResponseEntity.ok(savedCategory);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<?> deleteCategory(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Optional<Category> category = categoryRepository.findById(id);

        if (category.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        String categoryName = category.get().getName();

        categoryRepository.deleteById(id);

        auditLogService.log(
                authentication,
                "DELETE_CATEGORY",
                "Deleted category: " + categoryName + " (ID " + id + ")"
        );

        return ResponseEntity.ok().build();
    }

    @PostMapping("/categories/{categoryId}/subjects")
    public ResponseEntity<?> addSubjectToCategory(
            @PathVariable Long categoryId,
            @RequestBody SubjectOption subject,
            Authentication authentication
    ) {
        return categoryRepository.findById(categoryId)
                .map(category -> {
                    if (category.getSubjects() == null) {
                        category.setSubjects(new ArrayList<>());
                    }

                    category.getSubjects().add(subject);
                    Category savedCategory = categoryRepository.save(category);

                    auditLogService.log(
                            authentication,
                            "CREATE_SUBJECT",
                            "Added subject '" + subject.getName() + "' to category '" + category.getName() + "'"
                    );

                    return ResponseEntity.ok(savedCategory);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/subjects/{id}")
    public ResponseEntity<SubjectOption> updateSubject(
            @PathVariable Long id,
            @RequestBody SubjectOption updatedSubject,
            Authentication authentication
    ) {
        return subjectOptionRepository.findById(id)
                .map(subject -> {
                    String oldName = subject.getName();

                    subject.setName(updatedSubject.getName());
                    subject.setGradeIiiHours(updatedSubject.getGradeIiiHours());
                    subject.setGradeIvHours(updatedSubject.getGradeIvHours());
                    subject.setGroupName(updatedSubject.getGroupName());

                    SubjectOption savedSubject = subjectOptionRepository.save(subject);

                    auditLogService.log(
                            authentication,
                            "UPDATE_SUBJECT",
                            "Updated subject ID " + id + " from '" + oldName + "' to '" + savedSubject.getName() + "'"
                    );

                    return ResponseEntity.ok(savedSubject);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/subjects/{id}")
    public ResponseEntity<?> deleteSubject(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Optional<SubjectOption> subject = subjectOptionRepository.findById(id);

        if (subject.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        String subjectName = subject.get().getName();

        subjectOptionRepository.deleteById(id);

        auditLogService.log(
                authentication,
                "DELETE_SUBJECT",
                "Deleted subject: " + subjectName + " (ID " + id + ")"
        );

        return ResponseEntity.ok().build();
    }

    @GetMapping("/school-years")
    public List<SchoolYearOption> getSchoolYears() {
        return schoolYearOptionRepository.findAll();
    }

    @PostMapping("/school-years")
    public SchoolYearOption createSchoolYear(
            @RequestBody SchoolYearOption schoolYear,
            Authentication authentication
    ) {
        SchoolYearOption savedSchoolYear = schoolYearOptionRepository.save(schoolYear);

        auditLogService.log(
                authentication,
                "CREATE_SCHOOL_YEAR",
                "Created school year: " + savedSchoolYear.getLabel()
        );

        return savedSchoolYear;
    }

    @PutMapping("/school-years/{id}")
    public ResponseEntity<SchoolYearOption> updateSchoolYear(
            @PathVariable Long id,
            @RequestBody SchoolYearOption updatedSchoolYear,
            Authentication authentication
    ) {
        return schoolYearOptionRepository.findById(id)
                .map(schoolYear -> {
                    String oldLabel = schoolYear.getLabel();

                    schoolYear.setLabel(updatedSchoolYear.getLabel());
                    SchoolYearOption savedSchoolYear = schoolYearOptionRepository.save(schoolYear);

                    auditLogService.log(
                            authentication,
                            "UPDATE_SCHOOL_YEAR",
                            "Updated school year ID " + id + " from '" + oldLabel + "' to '" + savedSchoolYear.getLabel() + "'"
                    );

                    return ResponseEntity.ok(savedSchoolYear);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/school-years/{id}")
    public ResponseEntity<?> deleteSchoolYear(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Optional<SchoolYearOption> schoolYear = schoolYearOptionRepository.findById(id);

        if (schoolYear.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        String label = schoolYear.get().getLabel();

        schoolYearOptionRepository.deleteById(id);

        auditLogService.log(
                authentication,
                "DELETE_SCHOOL_YEAR",
                "Deleted school year: " + label + " (ID " + id + ")"
        );

        return ResponseEntity.ok().build();
    }
}