package com.studentplansystem.studyplangym.service;

import com.openai.client.OpenAIClient;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AiPlanningService {

    private final OpenAIClient openAIClient;
    private final String model;

    public AiPlanningService(
            OpenAIClient openAIClient,
            @Value("${app.ai.model}") String model
    ) {
        this.openAIClient = openAIClient;
        this.model = model;
    }

    public String testConnection() {
        ResponseCreateParams params = ResponseCreateParams.builder()
                .model(model)
                .input(
                        "You are the AI component of a university study planning system. " +
                        "Reply with exactly: AI planning assistant connected"
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

    public String getModel() {
        return model;
    }
}
