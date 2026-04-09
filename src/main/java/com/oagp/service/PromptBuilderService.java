package com.oagp.service;

import com.oagp.model.Scan;
import com.oagp.model.Violation;
import com.oagp.model.ViolationNode;
import org.springframework.stereotype.Service;

@Service
public class PromptBuilderService {

    /*
     * Builds one complete prompt for the entire scan.
     *
     * This method:
     * - adds scan details
     * - calculates a summary of violations by severity
     * - lists every violation in detail
     * - lists all affected elements (nodes) under each violation
     * - adds final instructions for the AI
     *
     *
     * Returns:
     * - one complete prompt string for the AI system
     */
    public String buildPromptForWholeScan(Scan scan) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("Accessibility Scan Prompt Data\n");
        prompt.append("==============================\n\n");

        prompt.append("Scan Details\n");
        prompt.append("------------\n");
        prompt.append("Audit Name: ").append(valueOrNA(scan.getAuditName())).append("\n");
        prompt.append("Page URL: ").append(valueOrNA(scan.getPageUrl())).append("\n");
        prompt.append("Scan Timestamp: ").append(valueOrNA(scan.getScanTimestamp())).append("\n");
        prompt.append("Time Zone: ").append(valueOrNA(scan.getTimeZone())).append("\n\n");

        // Prepare counters for summary data
        int totalViolations = 0;
        int criticalCount = 0;
        int seriousCount = 0;
        int moderateCount = 0;
        int minorCount = 0;

        // Calculate total number of violations and severity counts
        if (scan.getViolations() != null) {
            totalViolations = scan.getViolations().size();

            for (Violation violation : scan.getViolations()) {
                String impact = violation.getImpact();

                if (impact != null) {
                    switch (impact.toLowerCase()) {
                        case "critical":
                            criticalCount++;
                            break;
                        case "serious":
                            seriousCount++;
                            break;
                        case "moderate":
                            moderateCount++;
                            break;
                        case "minor":
                            minorCount++;
                            break;
                        default:
                            break;
                    }
                }
            }
        }

        // Add summary section
        prompt.append("Summary\n");
        prompt.append("-------\n");
        prompt.append("Total Violations: ").append(totalViolations).append("\n");
        prompt.append("Critical: ").append(criticalCount).append("\n");
        prompt.append("Serious: ").append(seriousCount).append("\n");
        prompt.append("Moderate: ").append(moderateCount).append("\n");
        prompt.append("Minor: ").append(minorCount).append("\n\n");

        prompt.append("Detailed Violations\n");
        prompt.append("-------------------\n\n");

        // If violations exist, write each one in detail
        if (scan.getViolations() != null && !scan.getViolations().isEmpty()) {
            int violationNumber = 1;

            for (Violation violation : scan.getViolations()) {
                prompt.append("Violation ").append(violationNumber).append("\n");
                prompt.append("-----------\n");
                prompt.append("Rule ID: ").append(valueOrNA(violation.getRuleId())).append("\n");
                prompt.append("Impact: ").append(valueOrNA(violation.getImpact())).append("\n");
                prompt.append("Description: ").append(valueOrNA(violation.getDescription())).append("\n");
                prompt.append("Help: ").append(valueOrNA(violation.getHelp())).append("\n");
                prompt.append("Tags: ").append(valueOrNA(violation.getTags())).append("\n");
                prompt.append("Help URL: ").append(valueOrNA(violation.getHelpUrl())).append("\n");
                prompt.append("Instance Count: ").append(valueOrNA(violation.getInstanceCount())).append("\n\n");

                prompt.append("Affected Elements:\n");

                // Add all node details for this violation
                if (violation.getNodes() != null && !violation.getNodes().isEmpty()) {
                    for (ViolationNode node : violation.getNodes()) {
                        prompt.append("- Message: ").append(valueOrNA(node.getMessage())).append("\n");
                        prompt.append("  HTML: ").append(valueOrNA(node.getHtml())).append("\n");
                        prompt.append("  Element Type: ").append(valueOrNA(node.getElementType())).append("\n\n");
                    }
                } else {
                    prompt.append("- No affected elements recorded\n\n");
                }

                violationNumber++;
            }
        } else {
            // If no violations exist, say so clearly
            prompt.append("No violations found.\n\n");
        }
        // Add final instructions for the AI

        prompt.append("Instructions for AI\n");
        prompt.append("-------------------\n");
        prompt.append("Using only the scan data above, write a clear accessibility report.\n");
        prompt.append("For each violation, explain:\n");
        prompt.append("1. What the issue is\n");
        prompt.append("2. Why it matters\n");
        prompt.append("3. How it can be fixed\n");
        prompt.append("Do not invent extra facts beyond the supplied scan data.\n");

        // Return the completed prompt text
        return prompt.toString();
    }

    /*
     * Helper method used to safely convert values to text.
     *
     * If a value is null, the method returns "N/A" instead.
     * This prevents null values from appearing directly in the prompt.
     */
    private String valueOrNA(Object value) {
        return value == null ? "N/A" : value.toString();
    }
}
