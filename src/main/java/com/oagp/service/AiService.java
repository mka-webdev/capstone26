package com.oagp.service;

import com.oagp.factory.AiAnswerStrategyFactory;
import com.oagp.model.AiProvider;
import com.oagp.model.AiTier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger log =
            LoggerFactory.getLogger(AiService.class);

    @Value("${ai.provider.default:GEMINI}")
    private AiProvider defaultProvider;

    @Value("${ai.tier.default:FREE}")
    private AiTier defaultTier;

    private final AiAnswerStrategyFactory factory;

    public AiService(AiAnswerStrategyFactory factory) {
        this.factory = factory;
    }

    public String generateRemediation(String prompt) {
        return generateRemediation(prompt, defaultProvider, defaultTier);
    }

    public String generateRemediation(String prompt, AiProvider provider) {
        return generateRemediation(prompt, provider, defaultTier);
    }

    public String generateRemediation(String prompt, AiProvider provider, AiTier tier) {
        log.info("Generating remediation with provider: {}, tier: {}, prompt: {}",
                provider, tier, prompt);
        try {
            return factory.getStrategy(provider, tier).ask(prompt);
        } catch (IllegalArgumentException e) {
            log.error("Failed to generate response with error: {}", e.getMessage());
            return e.getMessage();
        }
    }
}
