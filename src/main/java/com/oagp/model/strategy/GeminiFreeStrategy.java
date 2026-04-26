package com.oagp.model.strategy;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import com.oagp.model.AIProvider;
import com.oagp.model.AITier;
import org.apache.http.HttpException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class GeminiFreeStrategy implements AIAnswerStrategy {
    private final String apiKey;

    @Value("${gemini.model}")
    private String model;

    public GeminiFreeStrategy() {
        this.apiKey = System.getenv("GEMINI_API_KEY_FREE");
    }

    @Override
    public AIProvider provider() {
        return AIProvider.GEMINI;
    }

    @Override
    public AITier tier() {
        return AITier.FREE;
    }

    @Override
    public String ask(String question) throws IllegalArgumentException {
        if (apiKey == null || apiKey.isEmpty())
            throw new IllegalArgumentException("Gemini API key is missing. Please set the GEMINI_API_KEY_FREE environment variable.");

        Client client = Client.builder()
                              .apiKey(apiKey)
                              .build();

        GenerateContentResponse response = null;
        try {
            response = client.models.generateContent(
                    model,
                    question,
                    null
            );
        } catch (IOException | HttpException e) {
            return e.getMessage();
        }

        return response.text();
    }
}
