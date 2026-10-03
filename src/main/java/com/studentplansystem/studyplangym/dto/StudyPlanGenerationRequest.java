package com.studentplansystem.studyplangym.dto;

import java.time.LocalDate;

public class StudyPlanGenerationRequest {

    private String planName;
    private LocalDate startDate;
    private LocalDate endDate;

    public String getPlanName() {
        return planName;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}
