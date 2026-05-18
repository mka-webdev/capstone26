package com.oagp.service;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import com.oagp.model.Scan;
import com.oagp.model.Violation;
import com.oagp.model.ViolationNode;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Service;

@Service
public class PdfExportService {

    private final ScanService scanService;

    public PdfExportService(ScanService scanService) {
        this.scanService = scanService;
    }

    public byte[] generatePdfForScan(Long scanId) {
        Scan scan = scanService.getScanById(scanId);

        if (scan == null) {
            throw new IllegalArgumentException("Scan not found.");
        }

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 45, 45, 45, 45);
            PdfWriter.getInstance(document, outputStream);

            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 36);
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 28);
            Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);
            Font linkFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15, java.awt.Color.BLUE);
            Font tocItemFont = FontFactory.getFont(FontFactory.HELVETICA, 11, java.awt.Color.BLUE);
            Font violationTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 8);

            addTitlePage(document, scan, titleFont, subtitleFont, sectionFont, linkFont, tocItemFont, boldFont, normalFont);

            document.newPage();
            addSectionHeadingWithDestination(document, "Found Violations", "foundViolations", sectionFont);

            if (scan.getViolations() == null || scan.getViolations().isEmpty()) {
                addBodyParagraph(document, "No violations were recorded for this scan.", normalFont);
            } else {
                int count = 1;

                for (Violation violation : scan.getViolations()) {
                    String destination = "violation" + count;
                    addViolationBlock(document, violation, count, destination, violationTitleFont, boldFont, normalFont, smallFont);
                    count++;
                }
            }

            document.newPage();
            addSectionHeadingWithDestination(document, "AI Remediation Guide", "aiRemediation", sectionFont);

            String remediation = getRemediationFromScan(scan);

            if (remediation == null || remediation.isBlank()) {
                addBodyParagraph(document, "AI remediation has not been generated for this scan.", normalFont);
            } else {
                addRemediationText(document, cleanMarkdown(remediation), normalFont, boldFont);
            }

            document.close();
            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF.", e);
        }
    }

    private void addTitlePage(Document document,
            Scan scan,
            Font titleFont,
            Font subtitleFont,
            Font sectionFont,
            Font linkFont,
            Font tocItemFont,
            Font boldFont,
            Font normalFont) throws Exception {

        addTitle(document, "OAGP", titleFont);
        addTitle(document, "Accessibility Scan Report", subtitleFont);

        addSectionHeading(document, "Scan Details", sectionFont);

        addLabelValue(document, "Scan name", safe(scan.getAuditName()), boldFont, normalFont);
        addLabelValue(document, "Page URL", safe(scan.getPageUrl()), boldFont, normalFont);

        if (scan.getScanTimestamp() != null) {
            addLabelValue(
                    document,
                    "Scanned on",
                    scan.getScanTimestamp().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")),
                    boldFont,
                    normalFont
            );
        }

        int totalViolations = scan.getViolations() == null ? 0 : scan.getViolations().size();
        addLabelValue(document, "Total violations", String.valueOf(totalViolations), boldFont, normalFont);

        document.add(new Paragraph(" "));

        addSectionHeading(document, "Report Sections", sectionFont);
        addInternalLink(document, "Found Violations", "foundViolations", linkFont, 0, 0);

        if (scan.getViolations() != null && !scan.getViolations().isEmpty()) {
            int count = 1;

            for (Violation violation : scan.getViolations()) {
                addInternalLink(
                        document,
                        "Violation " + count + ": " + safe(violation.getRuleId()),
                        "violation" + count,
                        tocItemFont,
                        20,
                        0
                );
                count++;
            }
        }

        addInternalLink(document, "AI Remediation Guide", "aiRemediation", linkFont, 0, 18);
        String remediation = getRemediationFromScan(scan);

        if (remediation == null || remediation.isBlank()) {
            addBodyParagraph(document, "AI remediation has not been generated for this scan.", normalFont);
        }
    }

    private void addTitle(Document document, String text, Font font) throws Exception {
        Paragraph paragraph = new Paragraph(text, font);
        paragraph.setAlignment(Paragraph.ALIGN_CENTER);
        paragraph.setSpacingAfter(30);
        document.add(paragraph);
    }

    private void addSectionHeading(Document document, String text, Font font) throws Exception {
        Paragraph paragraph = new Paragraph(text, font);
        paragraph.setSpacingBefore(12);
        paragraph.setSpacingAfter(16);
        document.add(paragraph);
    }

    private void addSectionHeadingWithDestination(Document document,
            String text,
            String destination,
            Font font) throws Exception {

        Paragraph paragraph = new Paragraph();
        paragraph.setSpacingBefore(12);
        paragraph.setSpacingAfter(16);

        Chunk chunk = new Chunk(text, font);
        chunk.setLocalDestination(destination);

        paragraph.add(chunk);
        document.add(paragraph);
    }

    private void addViolationHeadingWithDestination(Document document,
            String text,
            String destination,
            Font font) throws Exception {

        Paragraph paragraph = new Paragraph();
        paragraph.setSpacingBefore(14);
        paragraph.setSpacingAfter(8);

        Chunk chunk = new Chunk(text, font);
        chunk.setLocalDestination(destination);

        paragraph.add(chunk);
        document.add(paragraph);
    }

    private void addInternalLink(Document document,
            String text,
            String destination,
            Font font,
            float indentationLeft,
            float spacingBefore) throws Exception {

        Paragraph paragraph = new Paragraph();
        paragraph.setIndentationLeft(indentationLeft);
        paragraph.setSpacingBefore(spacingBefore);
        paragraph.setSpacingAfter(8);

        Chunk chunk = new Chunk(text, font);
        chunk.setLocalGoto(destination);

        paragraph.add(chunk);
        document.add(paragraph);
    }

    private void addViolationBlock(Document document,
            Violation violation,
            int count,
            String destination,
            Font violationTitleFont,
            Font boldFont,
            Font normalFont,
            Font smallFont) throws Exception {

        addViolationHeadingWithDestination(
                document,
                "Violation " + count + ": " + safe(violation.getRuleId()),
                destination,
                violationTitleFont
        );

        addLabelValue(document, "Impact", safe(violation.getImpact()), boldFont, normalFont);
        addLabelValue(document, "Impacted users", safe(violation.getImpactedUsers()), boldFont, normalFont);
        addLabelValue(document, "Instances", safeNumber(violation.getInstanceCount()), boldFont, normalFont);
        addLabelValue(document, "Description", safe(violation.getDescription()), boldFont, normalFont);
        addLabelValue(document, "Help", safe(violation.getHelp()), boldFont, normalFont);
        addLabelValue(document, "Tags", safe(violation.getTags()), boldFont, normalFont);
        addLabelValue(document, "Help URL", safe(violation.getHelpUrl()), boldFont, normalFont);

        if (violation.getNodes() != null && !violation.getNodes().isEmpty()) {
            Paragraph affectedHeading = new Paragraph("Affected Elements", boldFont);
            affectedHeading.setSpacingBefore(10);
            affectedHeading.setSpacingAfter(5);
            document.add(affectedHeading);

            int nodeCount = 1;

            for (ViolationNode node : violation.getNodes()) {
                Paragraph elementHeading = new Paragraph("Element " + nodeCount, boldFont);
                elementHeading.setSpacingBefore(6);
                elementHeading.setSpacingAfter(3);
                document.add(elementHeading);

                addIndentedLine(document, "Message: " + safe(node.getMessage()), normalFont);
                addIndentedLine(document, "Element type: " + safe(node.getElementType()), normalFont);
                addIndentedLine(document, "HTML snippet: " + trimText(safe(node.getHtml()), 220), smallFont);

                nodeCount++;
            }
        }

        Paragraph spacer = new Paragraph(" ");
        spacer.setSpacingAfter(10);
        document.add(spacer);
    }

    private void addLabelValue(Document document, String label, String value, Font labelFont, Font valueFont) throws Exception {
        Paragraph paragraph = new Paragraph();
        paragraph.setSpacingAfter(4);
        paragraph.add(new Chunk(label + ": ", labelFont));
        paragraph.add(new Chunk(value, valueFont));
        document.add(paragraph);
    }

    private void addIndentedLine(Document document, String text, Font font) throws Exception {
        Paragraph paragraph = new Paragraph(text, font);
        paragraph.setIndentationLeft(18);
        paragraph.setSpacingAfter(3);
        document.add(paragraph);
    }

    private void addBodyParagraph(Document document, String text, Font font) throws Exception {
        Paragraph paragraph = new Paragraph(text, font);
        paragraph.setSpacingAfter(7);
        document.add(paragraph);
    }

    private void addRemediationText(Document document, String text, Font normalFont, Font boldFont) throws Exception {
        String[] lines = text.split("\\r?\\n");

        for (String line : lines) {
            String cleanedLine = line.trim();

            if (cleanedLine.isBlank()) {
                document.add(new Paragraph(" "));
            } else if (looksLikeHeading(cleanedLine)) {
                Paragraph heading = new Paragraph(cleanedLine, boldFont);
                heading.setSpacingBefore(10);
                heading.setSpacingAfter(5);
                document.add(heading);
            } else {
                addBodyParagraph(document, cleanedLine, normalFont);
            }
        }
    }

    private boolean looksLikeHeading(String line) {
        return line.endsWith(":")
                || line.startsWith("Accessibility Scan Report")
                || line.startsWith("Audit Name")
                || line.startsWith("Page URL")
                || line.startsWith("Scan Timestamp")
                || line.startsWith("Summary")
                || line.startsWith("Violation")
                || line.startsWith("Remediation")
                || line.startsWith("Recommendations")
                || line.startsWith("Conclusion");
    }

    private String getRemediationFromScan(Scan scan) {
        if (scan.getViolations() == null || scan.getViolations().isEmpty()) {
            return null;
        }

        return scan.getViolations().get(0).getRemediation();
    }

    private String cleanMarkdown(String text) {
        return text
                .replace("**", "")
                .replace("###", "")
                .replace("##", "")
                .replace("#", "")
                .replace("* ", "- ");
    }

    private String trimText(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }

        return text.substring(0, maxLength) + "...";
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "Not recorded" : value;
    }

    private String safeNumber(Integer value) {
        return value == null ? "0" : value.toString();
    }
}
