/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.oagp.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

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

    private static final String DEFAULT_IMPACT = "General users";

    private final Map<String, List<String>> impactMapping;

    public ImpactMappingService(ObjectMapper objectMapper) {
        this.impactMapping = loadImpactMapping(objectMapper);
    }

    public String getImpactedUsersText(String ruleId) {
        if (ruleId == null || ruleId.isBlank()) {
            return DEFAULT_IMPACT;
        }

        List<String> users = impactMapping.get(ruleId);

        if (users == null || users.isEmpty()) {
            return DEFAULT_IMPACT;
        }

        return String.join(", ", users);
    }

    private Map<String, List<String>> loadImpactMapping(ObjectMapper objectMapper) {
        try (InputStream inputStream = new ClassPathResource("impact-mapping.json").getInputStream()) {
            return objectMapper.readValue(
                    inputStream,
                    new TypeReference<Map<String, List<String>>>() {}
            );
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }
}