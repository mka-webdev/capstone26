package com.example.oagpfirst.dto;

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
    private TestEngine testEngine;
    private TestRunner testRunner;
    private String timestamp;
    private List<AxeViolation> violations;
    
    //Nested static class representing the "testEngine" object in JSON
    public static class TestEngine {
        private String name;
        private String version;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }
    }
    //Nested static class representing the "testRunner" object in JSON
    public static class TestRunner {
        private String name;
       

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public TestEngine getTestEngine() {
        return testEngine;
    }

    public void setTestEngine(TestEngine testEngine) {
        this.testEngine = testEngine;
    }

    public TestRunner getTestRunner() {
        return testRunner;
    }

    public void setTestRunner(TestRunner testRunner) {
        this.testRunner = testRunner;
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
