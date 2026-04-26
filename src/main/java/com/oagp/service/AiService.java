package com.oagp.service;

import com.oagp.factory.AIAnswerStrategyFactory;
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

    private final AIAnswerStrategyFactory factory;

    public AiService(AIAnswerStrategyFactory factory) {
        this.factory = factory;
    }

    public String generateRemediation(String prompt) {
        return generateRemediation(prompt, defaultProvider, defaultTier);
    }

    public String generateRemediation(String prompt, AIProvider provider) {
        return generateRemediation(prompt, provider, defaultTier);
    }

    public String generateRemediation(String prompt, AIProvider provider, AITier tier) {
        try {
            return factory.getStrategy(provider, tier).ask(prompt);
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
    }
}
