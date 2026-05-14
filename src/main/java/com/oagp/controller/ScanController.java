package com.oagp.controller;

import com.oagp.model.AiProvider;
import com.oagp.model.AiTier;
import com.oagp.model.Scan;
import com.oagp.service.MarkdownService;
import com.oagp.service.RemediationService;
import com.oagp.service.ScanService;
import com.oagp.service.ScannerProcessService;
import java.io.IOException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import org.springframework.web.bind.annotation.PathVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.oagp.service.PdfExportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@Controller
public class ScanController {

    private static final Logger log = LoggerFactory.getLogger(ScanController.class);

    private final ScanService scanService;
    private final ScannerProcessService scannerProcessService;
    private final RemediationService remediationService;
    private final MarkdownService markdownService;
    private final PdfExportService pdfExportService;

    public ScanController(ScanService scanService,
            ScannerProcessService scannerProcessService,
            RemediationService remediationService,
            MarkdownService markdownService,
            PdfExportService pdfExportService) {
        this.scanService = scanService;
        this.scannerProcessService = scannerProcessService;
        this.remediationService = remediationService;
        this.markdownService = markdownService;
        this.pdfExportService = pdfExportService;
    }

    @GetMapping("/")
    public String showNewScan(Model model) {
        log.debug("Navigating to new scan page");
        model.addAttribute("activePage", "scan");
        model.addAttribute("hasScans", scanService.getLatestScan() != null);
        return "new-scan";
    }

    @GetMapping("/results/latest")
    public String showLatestScan(Model model) {
        log.info("Retrieving latest scan for display");
        Scan latestScan = scanService.getLatestScan();
        model.addAttribute("scan", latestScan);
        model.addAttribute("activePage", "results");
        model.addAttribute("aiReportHtml", getAiReportHtml(latestScan));
        return "results";
    }

    @GetMapping("/scans")
    public String showAllScans(Model model) {
        log.info("Retrieving all scans for display");
        model.addAttribute("scans", scanService.getAllScans());
        model.addAttribute("activePage", "scans");
        return "scans";
    }

    @GetMapping("/scans/{id}/export-pdf")
    public ResponseEntity<byte[]> exportScanPdf(@PathVariable Long id) {
        log.info("Exporting PDF for scan id: {}", id);

        byte[] pdfBytes = pdfExportService.generatePdfForScan(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=oagp-scan-" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/scans/{id}/edit")
    public String showEditScanForm(@PathVariable Long id, Model model) {
        log.info("Showing edit form for scan id: {}", id);
        model.addAttribute("scans", scanService.getAllScans());
        model.addAttribute("activePage", "scans");
        model.addAttribute("editScanId", id);
        Scan scan = scanService.getScanById(id);
        model.addAttribute("editAuditName", scan != null ? scan.getAuditName() : "");
        return "scans";
    }

    @PostMapping("/scans/{id}/rename")
    public String renameScan(@PathVariable Long id,
            @RequestParam("auditName") String auditName,
            Model model) {
        log.info("Renaming scan id: {} to audit name: {}", id, auditName);
        if (auditName == null || auditName.isBlank()) {
            log.warn("Rename failed: audit name is empty for scan id: {}", id);
            model.addAttribute("errorMessage", "Scan name cannot be empty.");
            return showEditScanForm(id, model);
        }
        Scan updatedScan = scanService.updateScanName(id, auditName);
        if (updatedScan == null) {
            log.warn("Rename failed because scan id {} was not found", id);
            model.addAttribute("errorMessage", "Scan not found.");
            return showEditScanForm(id, model);
        }
        log.info("Successfully renamed scan id: {} to: {}", id, auditName);
        return "redirect:/scans";
    }

    @PostMapping("/scans/{id}/delete")
    public String deleteScan(@PathVariable Long id) {
        log.info("Deleting scan with id: {}", id);
        scanService.deleteScanById(id);
        log.info("Successfully deleted scan with id: {}", id);
        return "redirect:/scans";
    }

    @GetMapping("/results/{id}")
    public String showScanById(@PathVariable Long id, Model model) {
        log.info("Retrieving scan with id: {}", id);
        Scan scan = scanService.getScanById(id);
        if (scan == null) {
            log.warn("Scan not found with id: {}", id);
            model.addAttribute("errorMessage", "Scan not found.");
            model.addAttribute("activePage", "scans");
            model.addAttribute("scans", scanService.getAllScans());
            return "scans";
        }
        model.addAttribute("scan", scan);
        model.addAttribute("activePage", "results");
        model.addAttribute("aiReportHtml", getAiReportHtml(scan));
        return "results";
    }

    @PostMapping("/scan")
    public String runScan(@RequestParam("url") String url,
            @RequestParam("auditName") String auditName,
            Model model) {
        log.info("Starting scan for URL: {} with audit name: {}", url, auditName);
        try {
            String normalizedUrl = normalizeUrl(url);
            log.debug("Normalized URL: {}", normalizedUrl);
            validateUrl(normalizedUrl);

            Path jsonPath = scannerProcessService.runScan(normalizedUrl);
            log.debug("Scanner completed, processing JSON from: {}", jsonPath);
            scanService.processScannedJson(jsonPath, auditName);
            log.info("Successfully completed scan for URL: {}", normalizedUrl);

            return "redirect:/results/latest";

        } catch (IllegalArgumentException e) {
            log.warn("Invalid URL or request: {}", e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("activePage", "scan");
            return "new-scan";

        } catch (IOException | InterruptedException e) {
            log.error("Failed to scan the page: ", e);
            model.addAttribute("errorMessage", "Unable to scan the page.");
            model.addAttribute("activePage", "scan");
            return "new-scan";
        }
    }

    private void validateUrl(String url) {
        try {
            log.debug("Validating URL: {}", url);
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            String host = uri.getHost();

            if (scheme == null
                    || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                log.warn("Invalid URL scheme: {}", scheme);
                throw new IllegalArgumentException("URL must start with http or https.");
            }

            if (host == null || host.isBlank()) {
                log.warn("Invalid or missing host");
                throw new IllegalArgumentException("Invalid URL.");
            }

            if (!isValidHost(host)) {
                log.warn("Invalid host format: {}", host);
                throw new IllegalArgumentException(
                        "Enter a valid website address, for example example.com, www.example.com, or localhost:8080."
                );
            }

        } catch (URISyntaxException e) {
            log.warn("URL parsing failed: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid URL.");
        }
    }

    private String normalizeUrl(String url) {
        String trimmedUrl = url.trim();

        if (trimmedUrl.isBlank()) {
            log.warn("Blank URL input");
            throw new IllegalArgumentException("URL is required.");
        }

        if (trimmedUrl.startsWith("http://") || trimmedUrl.startsWith("https://")) {
            log.debug("URL already has protocol: {}", trimmedUrl);
            return trimmedUrl;
        }

        if (trimmedUrl.startsWith("localhost") || trimmedUrl.startsWith("127.0.0.1")) {
            String result = "http://" + trimmedUrl;
            log.debug("Added http:// to localhost/127.0.0.1: {}", result);
            return result;
        }

        if (trimmedUrl.startsWith("www.")) {
            String result = "https://" + trimmedUrl;
            log.debug("Added https:// to www. URL: {}", result);
            return result;
        }

        String result = "https://" + trimmedUrl;
        log.debug("Added default https:// to URL: {}", result);
        return result;
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
 * Handles the request to generate an AI report for the current scan.
 *
 * This method is called when the user presses the "Generate AI Report"
 * button on the front end. It retrieves the most recently saved scan
 * from the database. If a scan exists, it sends that scan to the
 * remediation service, which builds the prompt data and runs the
 * AI-report generation process.
 *
 * After the process completes, the method redirects the user back
 * to the home page so the latest scan page is shown again.
     */
    @PostMapping("/generate-report/{id}")
    public String generateReportForCurrentScan(
            @PathVariable Long id,
            @RequestParam(name = "aiChoice", required = false, defaultValue = "GEMINI_FREE") String aiChoice) {

        log.info("Generating AI report for scan id: {} with AI choice: {}", id, aiChoice);
        Scan scan = scanService.getScanById(id);

        if (scan != null) {
            AiProvider provider = AiProvider.GEMINI;
            AiTier tier = AiTier.FREE;

            if ("GEMINI_PAID".equals(aiChoice)) {
                tier = AiTier.PAID;
            } else if ("OPENAI_PAID".equals(aiChoice)) {
                provider = AiProvider.OPEN_AI;
                tier = AiTier.PAID;
            }

            log.debug("Generating remediations with provider: {}, tier: {}", provider, tier);
            remediationService.generateRemediationsForScan(scan, provider, tier);
            log.info("Successfully generated AI report for scan id: {}", id);
        } else {
            log.warn("Scan not found with id: {}, cannot generate report", id);
        }

        return "redirect:/results/" + id;
    }

    private String getAiReportHtml(Scan scan) {
        if (scan == null || scan.getViolations() == null || scan.getViolations().isEmpty()) {
            return null;
        }
        String remediation = scan.getViolations().get(0).getRemediation();
        if (remediation == null || remediation.isBlank()) {
            return null;
        }
        return markdownService.toHtml(remediation);
    }
}
