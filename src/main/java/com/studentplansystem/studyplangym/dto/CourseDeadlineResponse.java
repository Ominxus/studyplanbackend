package com.studentplansystem.studyplangym.dto;

import java.time.LocalDateTime;

public class CourseDeadlineResponse {

    private Long id;
    private Long courseId;
    private String courseName;
    private String title;
    private String deadlineType;
    private LocalDateTime dueAt;
    private Integer estimatedMinutes;
    private int importance;
    private String status;
    private String notes;

    public CourseDeadlineResponse(
            Long id,
            Long courseId,
            String courseName,
            String title,
            String deadlineType,
            LocalDateTime dueAt,
            Integer estimatedMinutes,
            int importance,
            String status,
            String notes
    ) {
        this.id = id;
        this.courseId = courseId;
        this.courseName = courseName;
        this.title = title;
        this.deadlineType = deadlineType;
        this.dueAt = dueAt;
        this.estimatedMinutes = estimatedMinutes;
        this.importance = importance;
        this.status = status;
        this.notes = notes;
    }

    public Long getId() {
        return id;
    }

    public Long getCourseId() {
        return courseId;
    }

    public String getCourseName() {
        return courseName;
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

    public int getImportance() {
        return importance;
    }

    public String getStatus() {
        return status;
    }

    public String getNotes() {
        return notes;
    }
}
