package com.studentplansystem.studyplangym.service;

import com.studentplansystem.studyplangym.entity.StudyPlan;
import com.studentplansystem.studyplangym.repository.StudyPlanRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudyPlanService {

    private final StudyPlanRepository repository;

    public StudyPlanService(StudyPlanRepository repository) {
        this.repository = repository;
    }

    public StudyPlan savePlan(StudyPlan plan) {
        return repository.save(plan);
    }

    public List<StudyPlan> getAllPlans() {
        return repository.findAll();
    }
}