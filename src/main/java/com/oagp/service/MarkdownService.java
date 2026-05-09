package com.oagp.service;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class MarkdownService {

    private static final Logger log = LoggerFactory.getLogger(MarkdownService.class);

    private final Parser parser = Parser.builder().build();
    private final HtmlRenderer renderer = HtmlRenderer.builder().escapeHtml(true).build();

    public String toHtml(String markdown) {
        if (markdown == null || markdown.isBlank()) {
            log.debug("Markdown is null or blank, returning empty string");
            return "";
        }
        log.debug("Converting markdown to HTML, markdown length: {}", markdown.length());
        Node document = parser.parse(markdown);
        String html = renderer.render(document);
        log.debug("Generated HTML, length: {}", html.length());
        return html;
    }
}
