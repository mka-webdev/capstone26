package com.oagp.controller;

import com.oagp.model.Scan;
import com.oagp.service.RemediationService;
import com.oagp.service.ScanService;
import com.oagp.service.ScannerProcessService;
import java.io.IOException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.oagp.model.ScanReport;
import com.oagp.repository.ScanReportRepository;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;

@Controller
public class ScanController {

    private final ScanService scanService;
    private final ScannerProcessService scannerProcessService;
    private final RemediationService remediationService;
    private final ScanReportRepository scanReportRepository;

    public ScanController(ScanService scanService,
            ScannerProcessService scannerProcessService,
            RemediationService remediationService,
            ScanReportRepository scanReportRepository) {
        this.scanService = scanService;
        this.scannerProcessService = scannerProcessService;
        this.remediationService = remediationService;
        this.scanReportRepository = scanReportRepository;
    }

    @GetMapping("/")
    public String showLatestScan(@RequestParam(value = "recentScan", required = false) boolean recentScan, Model model) {
        if (recentScan) {
            Scan latestScan = scanService.getLatestScan();
            model.addAttribute("scan", latestScan);

            if (latestScan != null) {
                ScanReport report = scanReportRepository.findByScanId(latestScan.getId()).orElse(null);
                model.addAttribute("scanReport", report);
            }
        }
        return "output";
    }

    @PostMapping("/scan")
    public String runScan(@RequestParam("url") String url,
            @RequestParam("auditName") String auditName,
            Model model) {
        try {
            String normalizedUrl = normalizeUrl(url);
            validateUrl(normalizedUrl);

            Path jsonPath = scannerProcessService.runScan(normalizedUrl);

            scanService.processScannedJson(jsonPath, auditName);

            return "redirect:/?recentScan=true";

        } catch (IllegalArgumentException e) {
            model.addAttribute("scan", scanService.getLatestScan());
            model.addAttribute("errorMessage", e.getMessage());
            return "output";

        } catch (IOException | InterruptedException e) {
            model.addAttribute("scan", scanService.getLatestScan());
            model.addAttribute("errorMessage", "Unable to scan the page.");
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

            if (!isValidHost(host)) {
                throw new IllegalArgumentException(
                        "Enter a valid website address, for example example.com, www.example.com, or localhost:8080."
                );
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

    private boolean isValidHost(String host) {
        if (host.equalsIgnoreCase("localhost")) {
            return true;
        }

        if (host.matches("^\\d{1,3}(\\.\\d{1,3}){3}$")) {
            return true;
        }

        return host.contains(".");
    }

    /*
 * Handles the normal application request to generate an AI report
     * for the latest saved scan.
     *
     * This endpoint is intended for form or front-end POST requests.
     * It retrieves the most recently saved scan, passes it to the
     * remediation service, and redirects back to the latest scan view.
     */
    @PostMapping("/generate-report")
    public String generateReportForCurrentScan() {
        // Retrieve the most recently saved scan from the database.
        Scan scan = scanService.getLatestScan();
        // Only continue if a scan was found.
        if (scan != null) {
            // Pass the current scan to the remediation service.
            // That service is responsible for building the full prompt
            // and running the AI-report generation logic.
            remediationService.generateRemediationsForScan(scan);
        }
        // Redirect the browser back to the latest scan view.
        return "redirect:/?recentScan=true";
    }

    /*
 * Temporary browser-test endpoint for generating an AI remediation report.
     *
     * This allows the AI report generation flow to be tested by entering a URL
     * directly in the browser, without adding a front-end button or form.
     *
     * Remove this endpoint before merging into main.
     */
    @GetMapping("/test-generate-report")
    public String testGenerateReportForCurrentScan() {
        Scan scan = scanService.getLatestScan();

        if (scan != null) {
            remediationService.generateRemediationsForScan(scan);
        }

        return "redirect:/?recentScan=true";
    }

}
