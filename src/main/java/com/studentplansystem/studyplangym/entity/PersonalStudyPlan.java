package com.studentplansystem.studyplangym.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "personal_study_plan")
public class PersonalStudyPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_plan_id")
    private PersonalStudyPlan sourcePlan;

    @Column(name = "plan_name", length = 150)
    private String planName;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(nullable = false, length = 30)
    private String status = "DRAFT";

    @Column(
            name = "generation_method",
            nullable = false,
            length = 30
    )
    private String generationMethod = "MANUAL";

    @Column(columnDefinition = "TEXT")
    private String summary;

    @CreationTimestamp
    @Column(
            name = "generated_at",
            updatable = false
    )
    private LocalDateTime generatedAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public PersonalStudyPlan getSourcePlan() {
        return sourcePlan;
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setSourcePlan(
            PersonalStudyPlan sourcePlan
    ) {
        this.sourcePlan = sourcePlan;
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

    public void setStatus(String status) {
        this.status = status;
    }

    public void setGenerationMethod(
            String generationMethod
    ) {
        this.generationMethod = generationMethod;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}
