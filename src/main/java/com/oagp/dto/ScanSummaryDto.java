package com.oagp.dto;

import java.time.LocalDateTime;

/**
 * ===============================================================
 * ScanSummaryDto
 * ===============================================================
 *
 * PURPOSE: This DTO provides a lightweight summary of a Scan, designed for
 * situations where a full scan (including all violations and nodes)
 * is not required.
 *
 * WHY THIS DTO IS NEEDED:
 * -----------------------
 * Returning the full ScanResponseDto for every request can be expensive
 * because it includes:
 *
 *   - All violations
 *   - All violation nodes
 *
 * This can lead to:
 *
 *   - Large payload sizes
 *   - Slow API responses
 *   - Poor frontend performance
 *
 * ScanSummaryDto solves this by returning only essential information.
 *
 * DATA INCLUDED:
 * --------------
 * This DTO contains:
 *
 *   - Basic scan metadata (ID, audit name, URL)
 *   - Timestamp and timezone
 *   - Summary statistics (total violations and severity breakdown)
 *
 * ===============================================================
 */

public class ScanSummaryDto {

    private Long id;
    private String auditName;
    private String pageUrl;
    private LocalDateTime scanTimestamp;
    private String timeZone;
    private Integer totalViolations;

    public ScanSummaryDto(Long id,
                          String auditName,
                          String pageUrl,
                          LocalDateTime scanTimestamp,
                          String timeZone,
                          Integer totalViolations) {
        this.id = id;
        this.auditName = auditName;
        this.pageUrl = pageUrl;
        this.scanTimestamp = scanTimestamp;
        this.timeZone = timeZone;
        this.totalViolations = totalViolations;
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

    public Integer getTotalViolations() {
        return totalViolations;
    }
}