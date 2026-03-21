package com.example.oagpfirst.controller;

import com.example.oagpfirst.model.Scan;
import com.example.oagpfirst.service.ScanService;
import com.example.oagpfirst.service.ScannerProcessService;
import java.io.IOException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;

@Controller
public class ScanController {

    private final ScanService scanService;
    private final ScannerProcessService scannerProcessService;

    public ScanController(ScanService scanService, ScannerProcessService scannerProcessService) {
        this.scanService = scanService;
        this.scannerProcessService = scannerProcessService;
    }

    @GetMapping("/")
    public String showLatestScan(Model model) {
        Scan latestScan = scanService.getLatestScan();
        model.addAttribute("scan", latestScan);
        return "output";
    }

    @PostMapping("/scan")
    public String runScan(@RequestParam("url") String url, Model model) {
        try {
            String normalizedUrl = normalizeUrl(url);
            validateUrl(normalizedUrl);

            Path jsonPath = scannerProcessService.runScan(normalizedUrl);
            scanService.processScannedJson(jsonPath);

            return "redirect:/";

        } catch (IllegalArgumentException e) {
            model.addAttribute("scan", scanService.getLatestScan());
            model.addAttribute("errorMessage", e.getMessage());
            return "output";

        } catch (IOException | InterruptedException e) {
            model.addAttribute("scan", scanService.getLatestScan());
            model.addAttribute("errorMessage", "Scanner failed: " + e.getMessage());
            return "output";
        }
    }

    private void validateUrl(String url) {
        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            String host = uri.getHost();

            if (scheme == null
                    || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                throw new IllegalArgumentException("URL must start with http or https.");
            }

            if (host == null || host.isBlank()) {
                throw new IllegalArgumentException("Invalid URL.");
            }

        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URL.");
        }
    }

    private String normalizeUrl(String url) {
        String trimmedUrl = url.trim();

        if (trimmedUrl.isBlank()) {
            throw new IllegalArgumentException("URL is required.");
        }

        if (trimmedUrl.startsWith("http://") || trimmedUrl.startsWith("https://")) {
            return trimmedUrl;
        }

        if (trimmedUrl.startsWith("localhost") || trimmedUrl.startsWith("127.0.0.1")) {
            return "http://" + trimmedUrl;
        }

        if (trimmedUrl.startsWith("www.")) {
            return "https://" + trimmedUrl;
        }

        return "https://" + trimmedUrl;
    }
}
