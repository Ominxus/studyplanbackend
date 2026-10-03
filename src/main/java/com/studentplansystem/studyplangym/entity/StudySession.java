package com.studentplansystem.studyplangym.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "study_session")
public class StudySession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private PersonalStudyPlan plan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_course_id")
    private StudentCourse studentCourse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deadline_id")
    private CourseDeadline deadline;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id")
    private StudentGoal goal;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @Column(name = "planned_minutes", nullable = false)
    private int plannedMinutes;

    @Column(name = "actual_minutes")
    private Integer actualMinutes;

    @Column(nullable = false, length = 30)
    private String status = "PLANNED";

    @Column(columnDefinition = "TEXT")
    private String rationale;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @CreationTimestamp
    @Column(
            name = "created_at",
            updatable = false
    )
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public PersonalStudyPlan getPlan() {
        return plan;
    }

    public StudentCourse getStudentCourse() {
        return studentCourse;
    }

    public CourseDeadline getDeadline() {
        return deadline;
    }

    public StudentGoal getGoal() {
        return goal;
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

    public String getNotes() {
        return notes;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setPlan(PersonalStudyPlan plan) {
        this.plan = plan;
    }

    public void setStudentCourse(
            StudentCourse studentCourse
    ) {
        this.studentCourse = studentCourse;
    }

    public void setDeadline(
            CourseDeadline deadline
    ) {
        this.deadline = deadline;
    }

    public void setGoal(StudentGoal goal) {
        this.goal = goal;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setStartAt(
            LocalDateTime startAt
    ) {
        this.startAt = startAt;
    }

    public void setEndAt(
            LocalDateTime endAt
    ) {
        this.endAt = endAt;
    }

    public void setPlannedMinutes(
            int plannedMinutes
    ) {
        this.plannedMinutes = plannedMinutes;
    }

    public void setActualMinutes(
            Integer actualMinutes
    ) {
        this.actualMinutes = actualMinutes;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setRationale(
            String rationale
    ) {
        this.rationale = rationale;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setCompletedAt(
            LocalDateTime completedAt
    ) {
        this.completedAt = completedAt;
    }
}
