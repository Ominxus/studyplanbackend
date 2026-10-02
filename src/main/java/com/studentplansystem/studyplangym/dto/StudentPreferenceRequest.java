package com.studentplansystem.studyplangym.dto;

public class StudentPreferenceRequest {

    private Integer preferredSessionMinutes;
    private Integer maximumSessionMinutes;
    private Integer breakMinutes;
    private String preferredStudyPeriod;
    private Integer maximumDailyMinutes;

    public Integer getPreferredSessionMinutes() {
        return preferredSessionMinutes;
    }

    public Integer getMaximumSessionMinutes() {
        return maximumSessionMinutes;
    }

    public Integer getBreakMinutes() {
        return breakMinutes;
    }

    public String getPreferredStudyPeriod() {
        return preferredStudyPeriod;
    }

    public Integer getMaximumDailyMinutes() {
        return maximumDailyMinutes;
    }

    public void setPreferredSessionMinutes(
            Integer preferredSessionMinutes
    ) {
        this.preferredSessionMinutes =
                preferredSessionMinutes;
    }

    public void setMaximumSessionMinutes(
            Integer maximumSessionMinutes
    ) {
        this.maximumSessionMinutes =
                maximumSessionMinutes;
    }

    public void setBreakMinutes(
            Integer breakMinutes
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
            Integer maximumDailyMinutes
    ) {
        this.maximumDailyMinutes =
                maximumDailyMinutes;
    }
}
