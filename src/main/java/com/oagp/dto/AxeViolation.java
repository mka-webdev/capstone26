package com.oagp.dto;

import java.util.List;
/*
 * DTO (Data Transfer Object)
 * 
 * This class represents a single violation from the axe-core JSON output.
 *
 * Each violation describes one type of accessibility issue found on the page.
 */
 

public class AxeViolation {

    private String id;
    private String impact;
    private String description;
    private String help;
    private List<String> tags;
    private String helpUrl;
    private List<NodeInfo> nodes;

    //Nested static class representing each affected HTML element (node)
    public static class NodeInfo {
        private String failureSummary;
        private String html;
        private List<String> target;

        public String getFailureSummary() {
            return failureSummary;
        }

        public void setFailureSummary(String failureSummary) {
            this.failureSummary = failureSummary;
        }

        public String getHtml() {
            return html;
        }

        public void setHtml(String html) {
            this.html = html;
        }

        public List<String> getTarget() {
            return target;
        }

        public void setTarget(List<String> target) {
            this.target = target;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImpact() {
        return impact;
    }

    public void setImpact(String impact) {
        this.impact = impact;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHelp() {
        return help;
    }

    public void setHelp(String help) {
        this.help = help;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getHelpUrl() {
        return helpUrl;
    }

    public void setHelpUrl(String helpUrl) {
        this.helpUrl = helpUrl;
    }

    public List<NodeInfo> getNodes() {
        return nodes;
    }

    public void setNodes(List<NodeInfo> nodes) {
        this.nodes = nodes;
    }
}