package com.studentplansystem.studyplangym.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class StudentGoalResponse {

    private Long id;
    private String title;
    private String description;
    private LocalDate targetDate;
    private int priority;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public StudentGoalResponse(
            Long id,
            String title,
            String description,
            LocalDate targetDate,
            int priority,
            String status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.targetDate = targetDate;
        this.priority = priority;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getTargetDate() {
        return targetDate;
    }

    public int getPriority() {
        return priority;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
