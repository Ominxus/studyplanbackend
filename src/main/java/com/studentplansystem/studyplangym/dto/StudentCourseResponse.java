package com.studentplansystem.studyplangym.dto;

import java.time.LocalDateTime;

public class StudentCourseResponse {

    private Long id;
    private String name;
    private String code;
    private int difficulty;
    private int priority;
    private String notes;
    private boolean active;
    private LocalDateTime createdAt;

    public StudentCourseResponse(
            Long id,
            String name,
            String code,
            int difficulty,
            int priority,
            String notes,
            boolean active,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.difficulty = difficulty;
        this.priority = priority;
        this.notes = notes;
        this.active = active;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public int getPriority() {
        return priority;
    }

    public String getNotes() {
        return notes;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
