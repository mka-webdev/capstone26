/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.oagp.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/*
 * Service class
 *
 * Loads the static rule-to-user-group mapping from a JSON resource file
 * and provides lookup support for impacted user groups by axe rule ID.
 */
@Service
public class ImpactMappingService {

    private static final Logger log = LoggerFactory.getLogger(ImpactMappingService.class);
    private static final String DEFAULT_IMPACT = "General users";

    private final Map<String, List<String>> impactMapping;

    public ImpactMappingService(ObjectMapper objectMapper) {
        this.impactMapping = loadImpactMapping(objectMapper);
    }

    public String getImpactedUsersText(String ruleId) {
        if (ruleId == null || ruleId.isBlank()) {
            log.debug("Rule ID is null or blank, returning default impact");
            return DEFAULT_IMPACT;
        }

        List<String> users = impactMapping.get(ruleId);

        if (users == null || users.isEmpty()) {
            log.debug("No impact mapping found for rule ID: {}, returning default", ruleId);
            return DEFAULT_IMPACT;
        }

        String result = String.join(", ", users);
        log.debug("Found impact mapping for rule ID: {}: {}", ruleId, result);
        return result;
    }

    private Map<String, List<String>> loadImpactMapping(ObjectMapper objectMapper) {
        try (InputStream inputStream = new ClassPathResource("impact-mapping.json").getInputStream()) {
            Map<String, List<String>> mapping = objectMapper.readValue(
                    inputStream,
                    new TypeReference<>() {}
            );
            log.info("Successfully loaded impact mapping with {} entries", mapping.size());
            return mapping;
        } catch (Exception e) {
            log.error("Failed to load impact mapping from impact-mapping.json: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }
}