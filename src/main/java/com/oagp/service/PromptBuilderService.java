package com.oagp.service;

import com.oagp.model.Scan;
import com.oagp.model.Violation;
import com.oagp.model.ViolationNode;
import org.springframework.stereotype.Service;

@Service
public class PromptBuilderService {

    /*
     * Builds one complete internal prompt for the entire scan.
     *
     * This method:
     * - adds scan details
     * - calculates a summary of violations by severity
     * - lists every violation in detail
     * - lists all affected elements (nodes) under each violation
     * - adds final formatting and content instructions for the AI
     *
     * Returns:
     * - one complete internal prompt string for the AI system
     *
     * This prompt is not intended to be displayed to users.
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

        // Add final internal requirements for the AI response.
        // These requirements shape the generated report but should not appear in the final report.

        prompt.append("\nFinal response requirements:\n");
        prompt.append("Write one user-facing accessibility remediation report.\n");
        prompt.append("Use only the scan data above.\n");
        prompt.append("Create one section for each violation listed above.\n");
        prompt.append("Do not include prompt instructions, scan prompt labels, internal notes, or raw scan data headings in the final response.\n\n");

        prompt.append("Required final report format:\n\n");
        prompt.append("Accessibility Remediation Report\n\n");

        prompt.append("Violation 1: [Rule ID]\n");
        prompt.append("Description: [One short sentence describing the issue.]\n");
        prompt.append("Impact: [One short paragraph explaining who is affected and why it matters.]\n");
        prompt.append("Remediation Clarification: [Two to three plain-text paragraphs explaining the issue in more detail, what the remediation means in practical terms, and what the developer or auditor should check.]\n");
        prompt.append("Recommendation: [One short practical paragraph explaining the direct fix.]\n\n");

        prompt.append("Violation 2: [Rule ID]\n");
        prompt.append("Description: [One short sentence describing the issue.]\n");
        prompt.append("Impact: [One short paragraph explaining who is affected and why it matters.]\n");
        prompt.append("Remediation Clarification: [Two to three plain-text paragraphs explaining the issue in more detail, what the remediation means in practical terms, and what the developer or auditor should check.]\n");
        prompt.append("Recommendation: [One short practical paragraph explaining the direct fix.]\n\n");

        prompt.append("Rules:\n");
        prompt.append("- Use plain text only.\n");
        prompt.append("- Do not use Markdown.\n");
        prompt.append("- Do not use bullet points.\n");
        prompt.append("- Do not use asterisks.\n");
        prompt.append("- Do not use code blocks.\n");
        prompt.append("- Do not add introductory or closing text.\n");
        prompt.append("- Do not include prompt instructions, scan prompt labels, internal notes, or raw scan data headings in the final response.\n");
        prompt.append("- Keep Description short.\n");
        prompt.append("- Keep Impact concise.\n");
        prompt.append("- Add Remediation Clarification before Recommendation for each violation.\n");
        prompt.append("- Make Remediation Clarification the most detailed part of each violation section.\n");
        prompt.append("- Remediation Clarification should contain two to three paragraphs per violation.\n");
        prompt.append("- Keep Recommendation short, practical, and action-focused.\n");
        prompt.append("- Do not invent extra facts beyond the supplied scan data.\n");

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
