package com.studentplansystem.studyplangym.dto;

import java.time.LocalTime;

public class StudentAvailabilityRequest {

    private Integer dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;

    public Integer getDayOfWeek() {
        return dayOfWeek;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setDayOfWeek(Integer dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }
}

