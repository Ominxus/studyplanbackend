package com.studentplansystem.studyplangym.dto;

import java.time.LocalTime;

public class StudentAvailabilityResponse {

    private Long id;
    private int dayOfWeek;
    private String dayName;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean active;

    public StudentAvailabilityResponse(
            Long id,
            int dayOfWeek,
            String dayName,
            LocalTime startTime,
            LocalTime endTime,
            boolean active
    ) {
        this.id = id;
        this.dayOfWeek = dayOfWeek;
        this.dayName = dayName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public String getDayName() {
        return dayName;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public boolean isActive() {
        return active;
    }
}
