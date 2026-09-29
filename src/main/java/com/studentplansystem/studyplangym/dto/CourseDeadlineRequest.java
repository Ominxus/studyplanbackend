package com.studentplansystem.studyplangym.dto;

import java.time.LocalDateTime;

public class CourseDeadlineRequest {

    private Long courseId;
    private String title;
    private String deadlineType;
    private LocalDateTime dueAt;
    private Integer estimatedMinutes;
    private Integer importance;
    private String status;
    private String notes;

    public Long getCourseId() {
        return courseId;
    }

    public String getTitle() {
        return title;
    }

    public String getDeadlineType() {
        return deadlineType;
    }

    public LocalDateTime getDueAt() {
        return dueAt;
    }

    public Integer getEstimatedMinutes() {
        return estimatedMinutes;
    }

    public Integer getImportance() {
        return importance;
    }

    public String getStatus() {
        return status;
    }

    public String getNotes() {
        return notes;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDeadlineType(String deadlineType) {
        this.deadlineType = deadlineType;
    }

    public void setDueAt(LocalDateTime dueAt) {
        this.dueAt = dueAt;
    }

    public void setEstimatedMinutes(Integer estimatedMinutes) {
        this.estimatedMinutes = estimatedMinutes;
    }

    public void setImportance(Integer importance) {
        this.importance = importance;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
