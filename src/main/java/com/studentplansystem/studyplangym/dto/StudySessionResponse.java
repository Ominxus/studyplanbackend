package com.studentplansystem.studyplangym.dto;

import java.time.LocalDateTime;

public class StudySessionResponse {

    private Long id;
    private Long courseId;
    private String courseName;
    private Long deadlineId;
    private String deadlineTitle;
    private Long goalId;
    private String goalTitle;
    private String title;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private int plannedMinutes;
    private Integer actualMinutes;
    private String status;
    private String rationale;
    private LocalDateTime completedAt;

    public StudySessionResponse(
            Long id,
            Long courseId,
            String courseName,
            Long deadlineId,
            String deadlineTitle,
            Long goalId,
            String goalTitle,
            String title,
            LocalDateTime startAt,
            LocalDateTime endAt,
            int plannedMinutes,
            Integer actualMinutes,
            String status,
            String rationale,
            LocalDateTime completedAt
    ) {
        this.id = id;
        this.courseId = courseId;
        this.courseName = courseName;
        this.deadlineId = deadlineId;
        this.deadlineTitle = deadlineTitle;
        this.goalId = goalId;
        this.goalTitle = goalTitle;
        this.title = title;
        this.startAt = startAt;
        this.endAt = endAt;
        this.plannedMinutes = plannedMinutes;
        this.actualMinutes = actualMinutes;
        this.status = status;
        this.rationale = rationale;
        this.completedAt = completedAt;
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

    public Long getDeadlineId() {
        return deadlineId;
    }

    public String getDeadlineTitle() {
        return deadlineTitle;
    }

    public Long getGoalId() {
        return goalId;
    }

    public String getGoalTitle() {
        return goalTitle;
    }

    public String getTitle() {
        return title;
    }

    public LocalDateTime getStartAt() {
        return startAt;
    }

    public LocalDateTime getEndAt() {
        return endAt;
    }

    public int getPlannedMinutes() {
        return plannedMinutes;
    }

    public Integer getActualMinutes() {
        return actualMinutes;
    }

    public String getStatus() {
        return status;
    }

    public String getRationale() {
        return rationale;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
}
