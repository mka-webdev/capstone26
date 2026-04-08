package com.oagp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import java.time.LocalDateTime;

@Entity
public class ScanReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "scan_id", nullable = false, unique = true)
    private Scan scan;

    private String reportTitle;

    @Column(length = 3000)
    private String summary;

    @Column(length = 50000)
    private String reportText;

    private Integer totalViolations;
    private Integer criticalCount;
    private Integer seriousCount;
    private Integer moderateCount;
    private Integer minorCount;

    private LocalDateTime generatedAt;

    public ScanReport() {
    }

    public Long getId() {
        return id;
    }

    public Scan getScan() {
        return scan;
    }

    public void setScan(Scan scan) {
        this.scan = scan;
    }

    public String getReportTitle() {
        return reportTitle;
    }

    public void setReportTitle(String reportTitle) {
        this.reportTitle = reportTitle;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getReportText() {
        return reportText;
    }

    public void setReportText(String reportText) {
        this.reportText = reportText;
    }

    public Integer getTotalViolations() {
        return totalViolations;
    }

    public void setTotalViolations(Integer totalViolations) {
        this.totalViolations = totalViolations;
    }

    public Integer getCriticalCount() {
        return criticalCount;
    }

    public void setCriticalCount(Integer criticalCount) {
        this.criticalCount = criticalCount;
    }

    public Integer getSeriousCount() {
        return seriousCount;
    }

    public void setSeriousCount(Integer seriousCount) {
        this.seriousCount = seriousCount;
    }

    public Integer getModerateCount() {
        return moderateCount;
    }

    public void setModerateCount(Integer moderateCount) {
        this.moderateCount = moderateCount;
    }

    public Integer getMinorCount() {
        return minorCount;
    }

    public void setMinorCount(Integer minorCount) {
        this.minorCount = minorCount;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }
}