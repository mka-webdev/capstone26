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

    private final Client freeClient;
    private final Client paidClient;

    @Value("${gemini.model}")
    private String model;

    public GeminiClient() {

        var freeKey = System.getenv("GEMINI_API_KEY_FREE");
        var paidKey = System.getenv("GEMINI_API_KEY_PAID");

        if (freeKey == null || paidKey == null) {
            throw new RuntimeException("Missing Gemini API keys");
        }

        this.freeClient = Client.builder()
                .apiKey(freeKey)
                .build();

        this.paidClient = Client.builder()
                .apiKey(paidKey)
                .build();
    }

    @Override
    public String ask(String prompt){
        return ask(prompt, AITier.FREE);
    }

    @Override
    public String ask(String prompt, AITier aiTier) {
        GenerateContentResponse response = null;
        try {

            var client = freeClient;
            if (aiTier == AITier.PAID) {
                client = paidClient;
            }
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
