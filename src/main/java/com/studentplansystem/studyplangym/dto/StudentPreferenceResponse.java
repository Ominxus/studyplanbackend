package com.studentplansystem.studyplangym.dto;

public class StudentPreferenceResponse {

    private Long id;
    private int preferredSessionMinutes;
    private int maximumSessionMinutes;
    private int breakMinutes;
    private String preferredStudyPeriod;
    private int maximumDailyMinutes;

    public StudentPreferenceResponse(
            Long id,
            int preferredSessionMinutes,
            int maximumSessionMinutes,
            int breakMinutes,
            String preferredStudyPeriod,
            int maximumDailyMinutes
    ) {
        this.id = id;
        this.preferredSessionMinutes =
                preferredSessionMinutes;
        this.maximumSessionMinutes =
                maximumSessionMinutes;
        this.breakMinutes = breakMinutes;
        this.preferredStudyPeriod =
                preferredStudyPeriod;
        this.maximumDailyMinutes =
                maximumDailyMinutes;
    }

    public Long getId() {
        return id;
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
}
