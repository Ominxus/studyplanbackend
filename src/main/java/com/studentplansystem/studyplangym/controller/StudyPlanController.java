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
    public StudyPlan createStudyPlan(@RequestBody StudyPlan studyPlan) {
        return repository.save(studyPlan);
    }

    @GetMapping
    public List<StudyPlan> getAllStudyPlans() {
        return repository.findAll();
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