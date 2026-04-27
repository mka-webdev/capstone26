package com.oagp.dto;

/**
 * ===============================================================
 * ViolationNodeResponseDto
 * ===============================================================
 *
 * PURPOSE:Represents a specific HTML element (node) that is affected
 * by an accessibility violation.
 *
 * ROLE IN SYSTEM: --------------- One Violation → Many Nodes
 *
 * Each node identifies:
 *
 * - The exact element causing the issue - The HTML snippet - A message
 * explaining the problem
 *
 * ===============================================================
 */

public class ViolationNodeResponseDto {

    private Long id;
    private String message;
    private String html;
    private String elementType;

    public ViolationNodeResponseDto(Long id, String message, String html, String elementType) {
        this.id = id;
        this.message = message;
        this.html = html;
        this.elementType = elementType;
    }

    public Long getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public String getHtml() {
        return html;
    }

    public String getElementType() {
        return elementType;
    }
}
