package com.oagp.service;

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

    /*
     * Generates a remediation response from the given prompt.
     *
     * In this version, the method:
     * - receives the full prompt text
     * - inserts that prompt into a sample response template
     * - returns the combined string
     *
     * Parameter:
     * - prompt: the structured text built from scan data
     *
     * Returns:
     * - a simulated AI response containing the prompt
     *
     * Future improvement:
     * - Replace this implementation with a real AI API call
     *   (e.g., OpenAI, Gemini, etc.)
     */
    public String generateRemediation(String prompt) {

        /*
         * Java text block is used here to define a multi-line string.
         *
         * %s is a placeholder that will be replaced with the prompt.
         * .formatted(prompt) inserts the prompt into the string.
         */
        return """
                SAMPLE AI RESPONSE

                The following remediation was generated from this prompt:

                %s
                """.formatted(prompt);
    }
}
