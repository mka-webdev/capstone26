package com.example.oagpfirst.dto;

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
    private List<NodeInfo> nodes;

    //Nested static class representing each affected HTML element (node)
    public static class NodeInfo {
        private List<String> target;

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

    public List<NodeInfo> getNodes() {
        return nodes;
    }

    public void setNodes(List<NodeInfo> nodes) {
        this.nodes = nodes;
    }
}
