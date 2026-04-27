package com.oagp.dto;
/**
 * ===============================================================
 * ScanResponseDto
 * ===============================================================
 *
 * PURPOSE: This DTO represents a complete accessibility scan
 * that is sent from the backend to the frontend via REST API.
 *
 * It is a simplified, safe version of the Scan entity that avoids exposing
 * internal database structure and prevents issues with recursive relationships.
 *
 * ===============================================================
 */

import java.time.LocalDateTime;
import java.util.List;


public class ScanResponseDto {

    private Long id;
    private String auditName;
    private String pageUrl;
    private LocalDateTime scanTimestamp;
    private String timeZone;
    private List<ViolationResponseDto> violations;

    public ScanResponseDto(Long id,
                           String auditName,
                           String pageUrl,
                           LocalDateTime scanTimestamp,
                           String timeZone,
                           List<ViolationResponseDto> violations) {
        this.id = id;
        this.auditName = auditName;
        this.pageUrl = pageUrl;
        this.scanTimestamp = scanTimestamp;
        this.timeZone = timeZone;
        this.violations = violations;
    }

    public Long getId() {
        return id;
    }

    public String getAuditName() {
        return auditName;
    }

    public String getPageUrl() {
        return pageUrl;
    }

    public LocalDateTime getScanTimestamp() {
        return scanTimestamp;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public List<ViolationResponseDto> getViolations() {
        return violations;
    }
}