package com.studentplansystem.studyplangym.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class PlanHistoryItemResponse {

    private Long id;
    private Long sourcePlanId;
    private String planName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private String generationMethod;
    private String summary;
    private LocalDateTime generatedAt;

    private int sessionCount;
    private int completedSessionCount;
    private int plannedMinutes;
    private int actualCompletedMinutes;

    public PlanHistoryItemResponse(
            Long id,
            Long sourcePlanId,
            String planName,
            LocalDate startDate,
            LocalDate endDate,
            String status,
            String generationMethod,
            String summary,
            LocalDateTime generatedAt,
            int sessionCount,
            int completedSessionCount,
            int plannedMinutes,
            int actualCompletedMinutes
    ) {
        this.id = id;
        this.sourcePlanId = sourcePlanId;
        this.planName = planName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.generationMethod = generationMethod;
        this.summary = summary;
        this.generatedAt = generatedAt;
        this.sessionCount = sessionCount;
        this.completedSessionCount = completedSessionCount;
        this.plannedMinutes = plannedMinutes;
        this.actualCompletedMinutes = actualCompletedMinutes;
    }

    public Long getId() {
        return id;
    }

    public Long getSourcePlanId() {
        return sourcePlanId;
    }

    public String getPlanName() {
        return planName;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public String getStatus() {
        return status;
    }

    public String getGenerationMethod() {
        return generationMethod;
    }

    public String getSummary() {
        return summary;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public int getSessionCount() {
        return sessionCount;
    }

    public int getCompletedSessionCount() {
        return completedSessionCount;
    }

    public int getPlannedMinutes() {
        return plannedMinutes;
    }

    public int getActualCompletedMinutes() {
        return actualCompletedMinutes;
    }
}
