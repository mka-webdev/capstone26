package com.example.oagpfirst.service;

import com.example.oagpfirst.dto.AxeResult;
import com.example.oagpfirst.dto.AxeViolation;
import com.example.oagpfirst.model.Scan;
import com.example.oagpfirst.model.Violation;
import com.example.oagpfirst.repository.ScanRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
                saveAxeResult(axeResult);
            }
        } else if (root.isObject()) {
            AxeResult axeResult = objectMapper.treeToValue(root, AxeResult.class);
            saveAxeResult(axeResult);
        } else {
            throw new IOException("Unsupported JSON format in results.json");
        }
    }
    //Converts DTO → Entity and saves it.
    private Scan saveAxeResult(AxeResult axeResult) {
        Scan scan = new Scan();
        scan.setUrl(axeResult.getUrl());

        if (axeResult.getTestEngine() != null) {
            scan.setTestEngineName(axeResult.getTestEngine().getName());
            scan.setTestEngineVersion(axeResult.getTestEngine().getVersion());
        }

        if (axeResult.getTestRunner() != null) {
            scan.setTestRunnerName(axeResult.getTestRunner().getName());
           
        }
        scan.setSourceTimestamp(axeResult.getTimestamp());  
        scan.setImportedAt(LocalDateTime.now());

        if (axeResult.getViolations() != null) {
            for (AxeViolation axeViolation : axeResult.getViolations()) {
                Violation violation = new Violation();
                violation.setRuleId(axeViolation.getId());
                violation.setImpact(axeViolation.getImpact());
                violation.setDescription(axeViolation.getDescription());
                violation.setHelp(axeViolation.getHelp());

                StringBuilder targets = new StringBuilder();
                if (axeViolation.getNodes() != null) {
                    for (AxeViolation.NodeInfo node : axeViolation.getNodes()) {
                        if (node.getTarget() != null) {
                            targets.append(String.join(", ", node.getTarget())).append(" | ");
                        }
                    }
                }

                violation.setTargetElements(targets.toString());
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
        List<Scan> scans = scanRepository.findAll();
        if (scans.isEmpty()) {
            return null;
        }
        return scans.get(scans.size() - 1);
    }
}