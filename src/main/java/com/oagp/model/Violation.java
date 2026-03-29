package com.oagp.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import java.util.ArrayList;
import java.util.List;

/*
 * entity class
 * 
 * This class represents a "Violation" table in the database.
 */
 

@Entity
public class Violation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ruleId;
    private String impact;

    @Column(length = 2000)
    private String description;

    @Column(length = 1000)
    private String help;

    @Column(length = 2000)
    private String tags;

    @Column(length = 1000)
    private String helpUrl;

    private Integer instanceCount;

    @ManyToOne
    @JoinColumn(name = "scan_id")
    private Scan scan;

    @OneToMany(mappedBy = "violation", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<ViolationNode> nodes = new ArrayList<>();

    public Violation() {
    }

    public void addNode(ViolationNode node) {
        nodes.add(node);
        node.setViolation(this);
    }

    public Long getId() {
        return id;
    }

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
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

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getHelpUrl() {
        return helpUrl;
    }

    public void setHelpUrl(String helpUrl) {
        this.helpUrl = helpUrl;
    }

    public Integer getInstanceCount() {
        return instanceCount;
    }

    public void setInstanceCount(Integer instanceCount) {
        this.instanceCount = instanceCount;
    }

    public Scan getScan() {
        return scan;
    }

    public void setScan(Scan scan) {
        this.scan = scan;
    }

    public List<ViolationNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<ViolationNode> nodes) {
        this.nodes = nodes;
    }
}