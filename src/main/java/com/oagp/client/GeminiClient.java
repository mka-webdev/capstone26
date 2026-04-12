package com.oagp.client;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import com.oagp.model.AITier;
import com.oagp.model.GenerativeAIClient;
import org.apache.http.HttpException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class GeminiClient implements GenerativeAIClient {

    private final String freeKey;
    private final String paidKey;

    @Value("${gemini.model}")
    private String model;

    public GeminiClient() {
        this.freeKey = System.getenv("GEMINI_API_KEY_FREE");
        this.paidKey = System.getenv("GEMINI_API_KEY_PAID");
    }

    @Override
    public String ask(String prompt) {
        return ask(prompt, AITier.FREE);
    }

    @Override
    public String ask(String prompt, AITier aiTier) {
        GenerateContentResponse response = null;
        try {
            String selectedKey;

            if (aiTier == AITier.PAID) {
                selectedKey = paidKey;
                if (selectedKey == null || selectedKey.isBlank()) {
                    return "Gemini paid API key is missing.";
                }
            } else {
                selectedKey = freeKey;
                if (selectedKey == null || selectedKey.isBlank()) {
                    return "Gemini free API key is missing.";
                }
            }

            Client client = Client.builder()
                    .apiKey(selectedKey)
                    .build();

            response = client.models.generateContent(
                    model,
                    prompt,
                    null
            );
        } catch (IOException e) {
            return e.getMessage();
        } catch (HttpException e) {
            return e.getMessage();
        }
        return response.text();
    }
}
