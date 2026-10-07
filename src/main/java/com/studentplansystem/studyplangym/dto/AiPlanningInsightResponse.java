package com.studentplansystem.studyplangym.dto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public class AiPlanningInsightResponse {

    @JsonPropertyDescription(
            "A concise personalized overview of the student's current academic situation."
    )
    public String academicOverview;

    @JsonPropertyDescription(
            "The single most important academic priority the student should focus on."
    )
    public String topPriority;

    @JsonPropertyDescription(
            "Three to five practical and personalized study recommendations."
    )
    public List<String> recommendations;

    @JsonPropertyDescription(
            "Potential planning risks such as approaching deadlines, insufficient study time, overloaded days, or unrealistic workload. Return an empty list when there are no significant risks."
    )
    public List<String> riskFlags;

    @JsonPropertyDescription(
            "A concise assessment of whether the student's current generated study plan matches their workload, deadlines, progress, availability, and preferences."
    )
    public String planAssessment;
}
