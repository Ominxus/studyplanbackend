package com.studentplansystem.studyplangym.service;

import com.openai.client.OpenAIClient;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.StructuredResponseCreateParams;
import com.studentplansystem.studyplangym.dto.AiPlanningInsightResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AiPlanningService {

    private final OpenAIClient openAIClient;
    private final AiPlanningContextService contextService;
    private final String model;

    public AiPlanningService(
            OpenAIClient openAIClient,
            AiPlanningContextService contextService,
            @Value("${app.ai.model}") String model
    ) {
        this.openAIClient = openAIClient;
        this.contextService = contextService;
        this.model = model;
    }

    public String testConnection() {

        ResponseCreateParams params =
                ResponseCreateParams.builder()
                        .model(model)
                        .input(
                                "You are the AI component of a university " +
                                "study planning system. Reply with exactly: " +
                                "AI planning assistant connected"
                        )
                        .build();

        Response response =
                openAIClient.responses().create(params);

        return response.output().stream()
                .flatMap(item -> item.message().stream())
                .flatMap(message -> message.content().stream())
                .flatMap(content -> content.outputText().stream())
                .map(outputText -> outputText.text())
                .findFirst()
                .orElse("No text response received.");
    }

    public AiPlanningInsightResponse getPlanningInsight(
            String username
    ) {

        String studentContext =
                contextService.buildContext(username);

        String prompt =
                """
                You are an AI study planning assistant inside a university
                personalized study planning system.

                Analyse the student's structured academic data below.

                Your role is advisory. The deterministic scheduling engine is
                responsible for hard scheduling constraints and for creating
                the actual timetable.

                Base every conclusion only on the supplied data.

                Do not invent courses, deadlines, goals, progress,
                availability, or study requirements.

                Consider:
                - deadline urgency;
                - deadline importance;
                - course priority and difficulty;
                - estimated remaining workload;
                - actual completed study time;
                - student goals;
                - weekly availability;
                - preferred study duration and study period;
                - current generated plan and progress.

                The academic overview should briefly explain the student's
                overall current situation.

                The top priority must identify the most important thing the
                student should focus on now and explain why.

                Give 3 to 5 practical recommendations.

                Risk flags must contain only genuine risks supported by the
                supplied information. If there are no meaningful risks,
                return an empty list.

                The plan assessment should evaluate the current generated
                study plan without attempting to replace the scheduling
                algorithm.

                Keep the response concise, useful, and student-friendly.

                STUDENT PLANNING DATA
                ---------------------
                """
                + studentContext;

        StructuredResponseCreateParams<AiPlanningInsightResponse> params =
                ResponseCreateParams.builder()
                        .model(model)
                        .input(prompt)
                        .text(AiPlanningInsightResponse.class)
                        .build();

        return openAIClient.responses()
                .create(params)
                .output()
                .stream()
                .flatMap(item -> item.message().stream())
                .flatMap(message -> message.content().stream())
                .flatMap(content -> content.outputText().stream())
                .findFirst()
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.BAD_GATEWAY,
                                "The AI planning assistant did not return an insight."
                        )
                );
    }

    public String getModel() {
        return model;
    }
}
