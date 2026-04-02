package com.oagp.dto;

import java.util.List;

/*
 * DTO (Data Transfer Object)
 * ---------------------------
 * This class represents the structure of the JSON output produced by axe-core.
 * It is used to:
 * 1. Deserialize (convert) JSON → Java object
 * 2. Temporarily hold scan data before saving it into the database
 */
 

public class AxeResult {

    private String url;
    private String timestamp;
    private List<AxeViolation> violations;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public List<AxeViolation> getViolations() {
        return violations;
    }

    public void setViolations(List<AxeViolation> violations) {
        this.violations = violations;
    }
}