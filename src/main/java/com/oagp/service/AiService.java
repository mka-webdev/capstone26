package com.oagp.service;

import com.oagp.client.GeminiClient;
import com.oagp.client.OpenAiClient;
import com.oagp.model.AIProvider;
import com.oagp.model.AITier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/*
 * Service class
 *
 * This class represents the AI layer of the system.
 *
 * It receives a prompt string (built from scan data) and returns
 * a response string that simulates an AI-generated accessibility report.
 *
 * In the current implementation, this class does NOT call a real AI API.
 * Instead, it returns a formatted sample response for testing purposes.
 *
 * This allows the full system workflow to be tested without requiring
 * external API integration.
 */
@Service
public class AiService {

    @Value("${ai.provider.default:GEMINI}")
    private AIProvider defaultProvider;

    @Value("${ai.tier.default:FREE}")
    private AITier defaultTier;

    private final GeminiClient geminiClient;
    private final OpenAiClient openAiClient;

    public AiService(GeminiClient geminiClient, OpenAiClient openAiClient) {
        this.geminiClient = geminiClient;
        this.openAiClient = openAiClient;
    }

    public String generateRemediation(String prompt) {
        return generateRemediation(prompt, defaultProvider, defaultTier);
    }

    public String generateRemediation(String prompt, AIProvider provider) {
        return generateRemediation(prompt, provider, defaultTier);
    }

    public String generateRemediation(String prompt, AIProvider provider, AITier tier) {

        switch (provider) {
            case GEMINI:
                return geminiClient.ask(prompt, tier);
            case OPEN_AI:
                return openAiClient.ask(prompt, tier);
            default:
                throw new IllegalArgumentException(
                        "Invalid provider: " + provider + ". Use 'GEMINI' or 'OPEN_AI'."
                );
        }
    }
}
