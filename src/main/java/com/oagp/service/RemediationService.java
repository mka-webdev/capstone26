package com.oagp.service;

import com.oagp.model.AiProvider;
import com.oagp.model.AiTier;
import com.oagp.model.Scan;
import com.oagp.model.Violation;
import com.oagp.repository.ViolationRepository;
import org.springframework.stereotype.Service;

/*
 * Service class
 *
 * This class coordinates the AI remediation process for a scan.
 *
 * It receives a completed Scan object, builds one full prompt
 * using the PromptBuilderService, sends that prompt to the
 * AiService, and saves the returned AI output into the
 * remediation field of each Violation.
 *
 * This class does not perform scanning itself and does not build
 * the prompt directly. Instead, it connects the prompt builder,
 * AI service, and repository save process.
 */
@Service
public class RemediationService {

    //Service used to build one complete prompt string from the scan data.
    private final PromptBuilderService promptBuilderService;
    //Service used to send the prompt to the AI system and return the response.
    private final AiService aiService;
    //Repository used to save updated Violation records back into the database.
    private final ViolationRepository violationRepository;

    public RemediationService(PromptBuilderService promptBuilderService,
            AiService aiService,
            ViolationRepository violationRepository) {
        this.promptBuilderService = promptBuilderService;
        this.aiService = aiService;
        this.violationRepository = violationRepository;

        /*
     * Generates remediation output for the given scan.
     *
     * This method:
     * - checks that the scan is valid and contains violations
     * - builds one full prompt for the whole scan
     * - prints the prompt to the console for testing
     * - sends the prompt to the AI service
     * - prints the AI response to the console
     * - saves the AI response into each Violation record
     *
     * Parameter:
     * - scan: the scan to process
         */
    }
    
    public void generateRemediationsForScan(Scan scan, AiProvider provider, AiTier tier) {
        if (scan == null || scan.getViolations() == null || scan.getViolations().isEmpty()) {
            return;
        }
        String fullPrompt = promptBuilderService.buildPromptForWholeScan(scan);

        System.out.println("====================================");
        System.out.println("FULL SCAN PROMPT DATA");
        System.out.println("====================================");
        System.out.println(fullPrompt);
        System.out.println("====================================");
        String aiResponse;
        if (provider != null && tier != null) {
            aiResponse = aiService.generateRemediation(fullPrompt, provider, tier);
        } else {
            aiResponse = aiService.generateRemediation(fullPrompt);
        }

        System.out.println("AI RESPONSE:");
        System.out.println(aiResponse);
        System.out.println("====================================");

         /*
         * Save the AI response into each violation.
         *
         * In the current design, one full AI report is generated for the whole scan,
         * so the same response is stored in every Violation record.
         */
        for (Violation violation : scan.getViolations()) {
            violation.setRemediation(aiResponse);
            violationRepository.save(violation);
        }
    }
}
