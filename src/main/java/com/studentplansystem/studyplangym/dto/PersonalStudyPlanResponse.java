package com.studentplansystem.studyplangym.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class PersonalStudyPlanResponse {

    private Long id;
    private String planName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private String generationMethod;
    private String summary;
    private LocalDateTime generatedAt;
    private List<StudySessionResponse> sessions;

    public PersonalStudyPlanResponse(
            Long id,
            String planName,
            LocalDate startDate,
            LocalDate endDate,
            String status,
            String generationMethod,
            String summary,
            LocalDateTime generatedAt,
            List<StudySessionResponse> sessions
    ) {
        this.id = id;
        this.planName = planName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.generationMethod = generationMethod;
        this.summary = summary;
        this.generatedAt = generatedAt;
        this.sessions = sessions;
    }

    public Long getId() { return id; }
    public String getPlanName() { return planName; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public String getStatus() { return status; }
    public String getGenerationMethod() { return generationMethod; }
    public String getSummary() { return summary; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public List<StudySessionResponse> getSessions() { return sessions; }
}
