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

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
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

    //Repository used to save and retrieve Scan data from the database
    private final ScanRepository scanRepository;

    /*
     * Jackson ObjectMapper used to:
     * Parse JSON files
     * -Convert JSON → Java objects
     */
    private final ObjectMapper objectMapper;

    public ScanService(ScanRepository scanRepository, ObjectMapper objectMapper) {
        this.scanRepository = scanRepository;
        this.objectMapper = objectMapper;
    }

    //Reads the JSON file and processes its contents.
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

    public Scan processScannedJson(Path jsonPath,String auditName) throws IOException {
        JsonNode root = objectMapper.readTree(jsonPath.toFile());

        if (!root.isObject()) {
            throw new IOException("Expected a single scan object in results.json");
        }

        AxeResult axeResult = objectMapper.treeToValue(root, AxeResult.class);
        return saveAxeResult(axeResult, auditName);
    }

    //Converts DTO → Entity and saves it.
    private Scan saveAxeResult(AxeResult axeResult, String auditName) {
        Scan scan = new Scan();
        scan.setAuditName(auditName);
        scan.setPageUrl(axeResult.getUrl());

        if (axeResult.getTimestamp() != null && !axeResult.getTimestamp().isBlank()) {
            OffsetDateTime offsetDateTime = OffsetDateTime.parse(axeResult.getTimestamp());
            scan.setScanTimestamp(offsetDateTime.toLocalDateTime());
            //scan.setTimeZone(offsetDateTime.getOffset().toString());
            scan.setTimeZone(ZoneId.systemDefault().getId());
        } else {
            scan.setScanTimestamp(LocalDateTime.now());
            //scan.setTimeZone("UTC");
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

        return scanRepository.save(scan);
    }

    //Returns all scan records from the database
    public List<Scan> getAllScans() {
        return scanRepository.findAll();
    }

    //Returns the most recently added scan
    public Scan getLatestScan() {
        return scanRepository.findTopByOrderByIdDesc().orElse(null);
    }
}