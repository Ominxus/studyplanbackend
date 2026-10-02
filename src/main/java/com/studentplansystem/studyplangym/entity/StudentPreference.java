package com.studentplansystem.studyplangym.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "student_preference")
public class StudentPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            unique = true
    )
    private User user;

    @Column(
            name = "preferred_session_minutes",
            nullable = false
    )
    private int preferredSessionMinutes = 60;

    @Column(
            name = "maximum_session_minutes",
            nullable = false
    )
    private int maximumSessionMinutes = 90;

    @Column(
            name = "break_minutes",
            nullable = false
    )
    private int breakMinutes = 15;

    @Column(
            name = "preferred_study_period",
            nullable = false,
            length = 20
    )
    private String preferredStudyPeriod = "ANY";

    @Column(
            name = "maximum_daily_minutes",
            nullable = false
    )
    private int maximumDailyMinutes = 180;

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

    public User getUser() {
        return user;
    }

    public int getPreferredSessionMinutes() {
        return preferredSessionMinutes;
    }

    public int getMaximumSessionMinutes() {
        return maximumSessionMinutes;
    }

    public int getBreakMinutes() {
        return breakMinutes;
    }

    public String getPreferredStudyPeriod() {
        return preferredStudyPeriod;
    }

    public int getMaximumDailyMinutes() {
        return maximumDailyMinutes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setPreferredSessionMinutes(
            int preferredSessionMinutes
    ) {
        this.preferredSessionMinutes =
                preferredSessionMinutes;
    }

    public void setMaximumSessionMinutes(
            int maximumSessionMinutes
    ) {
        this.maximumSessionMinutes =
                maximumSessionMinutes;
    }

    public void setBreakMinutes(
            int breakMinutes
    ) {
        this.breakMinutes = breakMinutes;
    }

    public void setPreferredStudyPeriod(
            String preferredStudyPeriod
    ) {
        this.preferredStudyPeriod =
                preferredStudyPeriod;
    }

    public void setMaximumDailyMinutes(
            int maximumDailyMinutes
    ) {
        this.maximumDailyMinutes =
                maximumDailyMinutes;
    }
}
