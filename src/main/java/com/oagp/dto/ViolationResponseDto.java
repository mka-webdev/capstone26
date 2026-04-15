package com.oagp.dto;

/**
 * ===============================================================
 * ViolationResponseDto
 * ===============================================================
 *
 * PURPOSE:Represents a single accessibility violation detected during
 * a scan.
 *
 * Each violation corresponds to a failed rule from the axe-core engine.
 *
 * ROLE IN SYSTEM: This DTO is part of the ScanResponseDto and
 * represents:
 *
 * One Violation → Many ViolationNodes
 *
 * ===============================================================
 */

import java.util.List;

public class ViolationResponseDto {

    private Long id;
    private String ruleId;
    private String impact;
    private String description;
    private String help;
    private String tags;
    private String helpUrl;
    private Integer instanceCount;
    private String remediation;
    private List<ViolationNodeResponseDto> nodes;

    public ViolationResponseDto(Long id,
            String ruleId,
            String impact,
            String description,
            String help,
            String tags,
            String helpUrl,
            Integer instanceCount,
            String remediation,
            List<ViolationNodeResponseDto> nodes) {
        this.id = id;
        this.ruleId = ruleId;
        this.impact = impact;
        this.description = description;
        this.help = help;
        this.tags = tags;
        this.helpUrl = helpUrl;
        this.instanceCount = instanceCount;
        this.remediation = remediation;
        this.nodes = nodes;
    }

    public Long getId() {
        return id;
    }

    public String getRuleId() {
        return ruleId;
    }

    public String getImpact() {
        return impact;
    }

    public String getDescription() {
        return description;
    }

    public String getHelp() {
        return help;
    }

    public String getTags() {
        return tags;
    }

    public String getHelpUrl() {
        return helpUrl;
    }

    public Integer getInstanceCount() {
        return instanceCount;
    }

    public String getRemediation() {
        return remediation;
    }

    public List<ViolationNodeResponseDto> getNodes() {
        return nodes;
    }
}
