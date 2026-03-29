package com.oagp.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/*
 * entity class
 *
 * This class represents a "Scan" table in the database.
 *
 * A Scan = one axe-core scan of a webpage.
 */
 

@Entity
public class Scan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String auditName;
    private String pageUrl;
    private LocalDateTime scanTimestamp;
    private String timeZone;

    @OneToMany(mappedBy = "scan", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<Violation> violations = new ArrayList<>();
    //Adds a violation to the scan and sets the relationship properly.
    public Scan() {
    }

    public void addViolation(Violation violation) {
        violations.add(violation);
        violation.setScan(this);
    }

    public Long getId() {
        return id;
    }

    public String getAuditName() {
        return auditName;
    }

    public void setAuditName(String auditName) {
        this.auditName = auditName;
    }

    public String getPageUrl() {
        return pageUrl;
    }

    public void setPageUrl(String pageUrl) {
        this.pageUrl = pageUrl;
    }

    public LocalDateTime getScanTimestamp() {
        return scanTimestamp;
    }

    public void setScanTimestamp(LocalDateTime scanTimestamp) {
        this.scanTimestamp = scanTimestamp;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public List<Violation> getViolations() {
        return violations;
    }

    public void setViolations(List<Violation> violations) {
        this.violations = violations;
    }
}