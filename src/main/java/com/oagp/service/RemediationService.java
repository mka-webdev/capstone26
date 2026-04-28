package com.oagp.service;

import com.oagp.model.Scan;
import com.oagp.model.ScanReport;
import com.oagp.repository.ScanReportRepository;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

/*
 * Service class
 *
 * This class coordinates the AI remediation process for a scan.
 *
 * It receives a completed Scan object, builds one full internal prompt
 * using the PromptBuilderService, sends that prompt to the AiService,
 * and saves only the returned AI response as a scan-level ScanReport.
 *
 * This class does not perform scanning itself and does not build the
 * prompt directly. Instead, it connects the prompt builder, AI service,
 * and scan report repository save process.
 */
@Service
public class RemediationService {

    //Service used to build one complete prompt string from the scan data.
    private final PromptBuilderService promptBuilderService;
    //Service used to send the prompt to the AI system and return the response.
    private final AiService aiService;
    //Repository used to save the generated scan-level AI report.
    private final ScanReportRepository scanReportRepository;

    public RemediationService(PromptBuilderService promptBuilderService,
            AiService aiService,
            ScanReportRepository scanReportRepository) {
        this.promptBuilderService = promptBuilderService;
        this.aiService = aiService;
        this.scanReportRepository = scanReportRepository;
    }

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
    public void generateRemediationsForScan(Scan scan) {
        // Stop immediately if no scan exists or if the scan has no violations.
        if (scan == null || scan.getViolations() == null || scan.getViolations().isEmpty()) {
            return;
        }
        // Build one full prompt string for the entire scan.
        String fullPrompt = promptBuilderService.buildPromptForWholeScan(scan);

        System.out.println("====================================");
        System.out.println("FULL SCAN PROMPT DATA");
        System.out.println("====================================");
        System.out.println(fullPrompt);
        System.out.println("====================================");
        // Send the prompt to the AI service and receive the response.
        String aiResponse = aiService.generateRemediation(fullPrompt);

        System.out.println("AI RESPONSE:");
        System.out.println(aiResponse);
        System.out.println("====================================");

        /*
         * Save only the AI-generated response as one scan-level report.
        *
        * The internal prompt is not saved as user-facing report content.
        * The report is linked to the Scan rather than duplicated across
        * individual Violation records.
         */
        ScanReport report = scanReportRepository.findByScanId(scan.getId())
                .orElseGet(ScanReport::new);

        report.setScan(scan);
        report.setReportTitle("AI Remediation Report for " + scan.getPageUrl());
        report.setReportText(aiResponse);
        report.setSummary("AI-generated remediation report for scan ID " + scan.getId());
        report.setTotalViolations(scan.getViolations().size());
        report.setGeneratedAt(LocalDateTime.now());

        scanReportRepository.save(report);
    }
}
