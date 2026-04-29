package com.oagp.service;

import com.oagp.dto.AxeResult;
import com.oagp.dto.AxeViolation;
import com.oagp.model.Scan;
import com.oagp.model.Violation;
import com.oagp.model.ViolationNode;
import com.oagp.repository.ScanRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import com.oagp.model.ScanReport;
import com.oagp.repository.ScanReportRepository;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.io.IOException;
import java.nio.file.Path;

/*
 * service class
 * Read JSON file axe-core output
 * Convert JSON → DTO objects
 * Convert DTO → Entity objects
 * Save data into the database
 * Provide data to controllers
 */
@Service
public class ScanService {

    // Repository used to save and retrieve Scan data from the database
    private final ScanRepository scanRepository;
    private final ScanReportRepository scanReportRepository;
    private final ImpactMappingService impactMappingService;

    /*
     * Jackson ObjectMapper used to:
     * Parse JSON files
     * Convert JSON → Java objects
     */
    private final ObjectMapper objectMapper;

    public ScanService(ScanRepository scanRepository,
            ScanReportRepository scanReportRepository,
            ObjectMapper objectMapper,
            ImpactMappingService impactMappingService) {
        this.scanRepository = scanRepository;
        this.scanReportRepository = scanReportRepository;
        this.objectMapper = objectMapper;
        this.impactMappingService = impactMappingService;
    }

    // Reads the JSON file and processes its contents.
    public void processJsonFile(Path jsonPath) throws IOException {
        JsonNode root = objectMapper.readTree(jsonPath.toFile());

        if (root.isArray()) {
            for (JsonNode node : root) {
                AxeResult axeResult = objectMapper.treeToValue(node, AxeResult.class);
                saveAxeResult(axeResult, "Imported Scan");
            }
        } else if (root.isObject()) {
            AxeResult axeResult = objectMapper.treeToValue(root, AxeResult.class);
            saveAxeResult(axeResult, "Imported Scan");
        } else {
            throw new IOException("Unsupported JSON format in results.json");
        }
    }

    public Scan processScannedJson(Path jsonPath, String auditName) throws IOException {
        JsonNode root = objectMapper.readTree(jsonPath.toFile());

        if (!root.isObject()) {
            throw new IOException("Expected a single scan object in results.json");
        }

        AxeResult axeResult = objectMapper.treeToValue(root, AxeResult.class);
        return saveAxeResult(axeResult, auditName);
    }

    // Converts DTO → Entity and saves it.
    private Scan saveAxeResult(AxeResult axeResult, String auditName) {
        Scan scan = new Scan();
        scan.setAuditName(auditName);
        scan.setPageUrl(axeResult.getUrl());

        if (axeResult.getTimestamp() != null && !axeResult.getTimestamp().isBlank()) {
            OffsetDateTime offsetDateTime = OffsetDateTime.parse(axeResult.getTimestamp());
            scan.setScanTimestamp(offsetDateTime.toLocalDateTime());
            scan.setTimeZone(ZoneId.systemDefault().getId());
        } else {
            scan.setScanTimestamp(LocalDateTime.now());
            scan.setTimeZone(ZoneId.systemDefault().getId());
        }

        if (axeResult.getViolations() != null) {
            for (AxeViolation axeViolation : axeResult.getViolations()) {
                Violation violation = new Violation();
                violation.setRuleId(axeViolation.getId());
                violation.setImpact(axeViolation.getImpact());
                violation.setDescription(axeViolation.getDescription());
                violation.setHelp(axeViolation.getHelp());
                violation.setHelpUrl(axeViolation.getHelpUrl());
                violation.setImpactedUsers(
                        impactMappingService.getImpactedUsersText(axeViolation.getId())
                );

                if (axeViolation.getTags() != null) {
                    violation.setTags(String.join(", ", axeViolation.getTags()));
                } else {
                    violation.setTags(null);
                }

                if (axeViolation.getNodes() != null) {
                    violation.setInstanceCount(axeViolation.getNodes().size());

                    for (AxeViolation.NodeInfo nodeInfo : axeViolation.getNodes()) {
                        ViolationNode violationNode = new ViolationNode();
                        violationNode.setMessage(nodeInfo.getFailureSummary());
                        violationNode.setHtml(nodeInfo.getHtml());

                        if (nodeInfo.getTarget() != null && !nodeInfo.getTarget().isEmpty()) {
                            violationNode.setElementType(nodeInfo.getTarget().get(0));
                        } else {
                            violationNode.setElementType("unknown");
                        }

                        violation.addNode(violationNode);
                    }
                } else {
                    violation.setInstanceCount(0);
                }

                scan.addViolation(violation);
            }
        }

        Scan savedScan = scanRepository.save(scan);
        buildAndSaveScanReport(savedScan);

        // Remediation is now triggered manually from the front-end button,
        // so it is NOT called automatically here.
        return savedScan;
    }

    private void buildAndSaveScanReport(Scan scan) {
        if (scan == null) {
            return;
        }

        ScanReport scanReport = new ScanReport();
        scanReport.setScan(scan);
        scanReport.setReportTitle("Accessibility Report for " + scan.getPageUrl());
        scanReport.setGeneratedAt(LocalDateTime.now());

        int criticalCount = 0;
        int seriousCount = 0;
        int moderateCount = 0;
        int minorCount = 0;

        StringBuilder reportTextBuilder = new StringBuilder();

        reportTextBuilder.append("Accessibility Scan Report\n");
        reportTextBuilder.append("=========================\n\n");

        reportTextBuilder.append("Scan Details\n");
        reportTextBuilder.append("------------\n");
        reportTextBuilder.append("Audit Name: ").append(scan.getAuditName()).append("\n");
        reportTextBuilder.append("Page URL: ").append(scan.getPageUrl()).append("\n");
        reportTextBuilder.append("Scan Timestamp: ").append(scan.getScanTimestamp()).append("\n");
        reportTextBuilder.append("Time Zone: ").append(scan.getTimeZone()).append("\n\n");

        int totalViolations = 0;

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

        String summary = "Total Violations: " + totalViolations
                + ", Critical: " + criticalCount
                + ", Serious: " + seriousCount
                + ", Moderate: " + moderateCount
                + ", Minor: " + minorCount;

        scanReport.setSummary(summary);
        scanReport.setTotalViolations(totalViolations);
        scanReport.setCriticalCount(criticalCount);
        scanReport.setSeriousCount(seriousCount);
        scanReport.setModerateCount(moderateCount);
        scanReport.setMinorCount(minorCount);

        reportTextBuilder.append("Summary\n");
        reportTextBuilder.append("-------\n");
        reportTextBuilder.append(summary).append("\n\n");

        reportTextBuilder.append("Detailed Violations\n");
        reportTextBuilder.append("-------------------\n\n");

        if (scan.getViolations() != null && !scan.getViolations().isEmpty()) {
            int violationNumber = 1;

            for (Violation violation : scan.getViolations()) {
                reportTextBuilder.append("Violation ").append(violationNumber).append("\n");
                reportTextBuilder.append("Rule ID: ").append(violation.getRuleId()).append("\n");
                reportTextBuilder.append("Impact: ").append(violation.getImpact()).append("\n");
                reportTextBuilder.append("Description: ").append(violation.getDescription()).append("\n");
                reportTextBuilder.append("Help: ").append(violation.getHelp()).append("\n");
                reportTextBuilder.append("Tags: ").append(violation.getTags()).append("\n");
                reportTextBuilder.append("Help URL: ").append(violation.getHelpUrl()).append("\n");
                reportTextBuilder.append("Instance Count: ").append(violation.getInstanceCount()).append("\n");

                reportTextBuilder.append("Affected Elements:\n");

                if (violation.getNodes() != null && !violation.getNodes().isEmpty()) {
                    for (ViolationNode node : violation.getNodes()) {
                        reportTextBuilder.append("- Message: ").append(node.getMessage()).append("\n");
                        reportTextBuilder.append("  HTML: ").append(node.getHtml()).append("\n");
                        reportTextBuilder.append("  Element Type: ").append(node.getElementType()).append("\n");
                    }
                } else {
                    reportTextBuilder.append("- No affected elements recorded\n");
                }

                reportTextBuilder.append("\n");
                violationNumber++;
            }
        } else {
            reportTextBuilder.append("No violations found.\n");
        }

        reportTextBuilder.append("End of Report\n");
        reportTextBuilder.append("=============\n");
        reportTextBuilder.append("Generated by OAGP System\n");

        scanReport.setReportText(reportTextBuilder.toString());

        scanReportRepository.save(scanReport);
    }

    // Returns all scan records from the database
    public List<Scan> getAllScans() {
        List<Scan> scans = scanRepository.findAll();
        scans.forEach(this::applyImpactedUsers);
        return scans;
    }

    // Returns the most recently added scan
    public Scan getLatestScan() {
        Scan scan = scanRepository.findTopByOrderByIdDesc().orElse(null);
        applyImpactedUsers(scan);
        return scan;
    }

    // Returns a scan by its database ID
    public Scan getScanById(Long id) {
        Scan scan = scanRepository.findById(id).orElse(null);
        applyImpactedUsers(scan);
        return scan;
    }

    public Scan updateScanName(Long id, String auditName) {
        Scan scan = scanRepository.findById(id).orElse(null);
        if (scan == null) {
            return null;
        }
        scan.setAuditName(auditName);
        return scanRepository.save(scan);
    }

    public void deleteScanById(Long id) {
        if (scanRepository.existsById(id)) {
            scanRepository.deleteById(id);
        }
    }
  
    private void applyImpactedUsers(Scan scan) {
        if (scan == null || scan.getViolations() == null) {
            return;
        }

        for (Violation violation : scan.getViolations()) {
            violation.setImpactedUsers(
                    impactMappingService.getImpactedUsersText(violation.getRuleId())
            );
        }
    }
}
