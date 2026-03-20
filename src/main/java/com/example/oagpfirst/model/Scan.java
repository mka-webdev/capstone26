package com.example.oagpfirst.model;

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

    private String url;
    private String testEngineName;
    private String testEngineVersion;
    private String testRunnerName;
    private String sourceTimestamp;
    private LocalDateTime importedAt;
    

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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTestEngineName() {
        return testEngineName;
    }

    public void setTestEngineName(String testEngineName) {
        this.testEngineName = testEngineName;
    }

    public String getTestEngineVersion() {
        return testEngineVersion;
    }

    public void setTestEngineVersion(String testEngineVersion) {
        this.testEngineVersion = testEngineVersion;
    }

    public String getTestRunnerName() {
        return testRunnerName;
    }

    public void setTestRunnerName(String testRunnerName) {
        this.testRunnerName = testRunnerName;
    }


    public String getSourceTimestamp() {
        return sourceTimestamp;
    }

    public void setSourceTimestamp(String sourceTimestamp) {
        this.sourceTimestamp = sourceTimestamp;
    }

    public LocalDateTime getImportedAt() {
        return importedAt;
    }

    public void setImportedAt(LocalDateTime importedAt) {
        this.importedAt = importedAt;
    }

    public List<Violation> getViolations() {
        return violations;
    }

    public void setViolations(List<Violation> violations) {
        this.violations = violations;
    }
}
