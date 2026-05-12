package com.studentplansystem.studyplangym.controller;

import com.studentplansystem.studyplangym.entity.Category;
import com.studentplansystem.studyplangym.entity.SubjectOption;
import com.studentplansystem.studyplangym.entity.SchoolYearOption;
import com.studentplansystem.studyplangym.repository.CategoryRepository;
import com.studentplansystem.studyplangym.repository.SubjectOptionRepository;
import com.studentplansystem.studyplangym.repository.SchoolYearOptionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/config")
    
public class StudyPlanConfigController {

    private final CategoryRepository categoryRepository;
    private final SubjectOptionRepository subjectOptionRepository;
    private final SchoolYearOptionRepository schoolYearOptionRepository;

    public StudyPlanConfigController(
            CategoryRepository categoryRepository,
            SubjectOptionRepository subjectOptionRepository,
            SchoolYearOptionRepository schoolYearOptionRepository
    ) {
        this.categoryRepository = categoryRepository;
        this.subjectOptionRepository = subjectOptionRepository;
        this.schoolYearOptionRepository = schoolYearOptionRepository;
    }

    @GetMapping("/categories")
    public List<Category> getCategories() {
        return categoryRepository.findAll();
    }

    @PostMapping("/categories")
    public Category createCategory(@RequestBody Category category) {
        return categoryRepository.save(category);
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<Category> updateCategory(
            @PathVariable Long id,
            @RequestBody Category updatedCategory
    ) {
        return categoryRepository.findById(id)
                .map(category -> {
                    category.setName(updatedCategory.getName());
                    category.setMinRequired(updatedCategory.getMinRequired());
                    category.setMaxAllowed(updatedCategory.getMaxAllowed());
                    category.setOptional(updatedCategory.isOptional());
                    return ResponseEntity.ok(categoryRepository.save(category));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        if (!categoryRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        categoryRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/categories/{categoryId}/subjects")
    public ResponseEntity<?> addSubjectToCategory(
            @PathVariable Long categoryId,
            @RequestBody SubjectOption subject
    ) {
        return categoryRepository.findById(categoryId)
                .map(category -> {
                    if (category.getSubjects() == null) {
                        category.setSubjects(new ArrayList<>());
                    }

                    category.getSubjects().add(subject);
                    Category savedCategory = categoryRepository.save(category);

                    return ResponseEntity.ok(savedCategory);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/subjects/{id}")
    public ResponseEntity<SubjectOption> updateSubject(
            @PathVariable Long id,
            @RequestBody SubjectOption updatedSubject
    ) {
        return subjectOptionRepository.findById(id)
                .map(subject -> {
                    subject.setName(updatedSubject.getName());
                    subject.setGradeIiiHours(updatedSubject.getGradeIiiHours());
                    subject.setGradeIvHours(updatedSubject.getGradeIvHours());
                    subject.setGroupName(updatedSubject.getGroupName());
                    return ResponseEntity.ok(subjectOptionRepository.save(subject));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/subjects/{id}")
    public ResponseEntity<?> deleteSubject(@PathVariable Long id) {
        if (!subjectOptionRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        subjectOptionRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/school-years")
    public List<SchoolYearOption> getSchoolYears() {
        return schoolYearOptionRepository.findAll();
    }

    @PostMapping("/school-years")
    public SchoolYearOption createSchoolYear(@RequestBody SchoolYearOption schoolYear) {
        return schoolYearOptionRepository.save(schoolYear);
    }

    @PutMapping("/school-years/{id}")
    public ResponseEntity<SchoolYearOption> updateSchoolYear(
            @PathVariable Long id,
            @RequestBody SchoolYearOption updatedSchoolYear
    ) {
        return schoolYearOptionRepository.findById(id)
                .map(schoolYear -> {
                    schoolYear.setLabel(updatedSchoolYear.getLabel());
                    return ResponseEntity.ok(schoolYearOptionRepository.save(schoolYear));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/school-years/{id}")
    public ResponseEntity<?> deleteSchoolYear(@PathVariable Long id) {
        if (!schoolYearOptionRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        schoolYearOptionRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}