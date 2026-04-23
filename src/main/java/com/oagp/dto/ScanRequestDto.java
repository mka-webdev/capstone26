package com.oagp.dto;

/*
 * Data Transfer Object used for incoming API requests
 * when creating or updating a scan.
 *
 * This class only includes fields that the user is allowed to provide.
 * System-generated fields such as ID, timestamp, violations, and nodes
 * are NOT included here.
 */


public class ScanRequestDto {

    private String auditName;
    private String url;

    public ScanRequestDto() {
    }

    public String getAuditName() {
        return auditName;
    }

    public void setAuditName(String auditName) {
        this.auditName = auditName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}