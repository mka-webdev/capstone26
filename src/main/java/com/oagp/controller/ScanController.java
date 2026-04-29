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

@Controller
public class ScanController {

    private final ScanService scanService;
    private final ScannerProcessService scannerProcessService;
    private final RemediationService remediationService;
    private final MarkdownService markdownService;

    public ScanController(ScanService scanService,
            ScannerProcessService scannerProcessService,
            RemediationService remediationService,
            MarkdownService markdownService) {
        this.scanService = scanService;
        this.scannerProcessService = scannerProcessService;
        this.remediationService = remediationService;
        this.markdownService = markdownService;
    }

    @GetMapping("/")
    public String showNewScan(Model model) {
        model.addAttribute("activePage", "scan");
        model.addAttribute("hasScans", scanService.getLatestScan() != null);
        return "new-scan";
    }
    
    @GetMapping("/results/latest")
    public String showLatestScan(Model model) {
        Scan latestScan = scanService.getLatestScan();
        model.addAttribute("scan", latestScan);
        model.addAttribute("activePage", "results");
        model.addAttribute("aiReportHtml", getAiReportHtml(latestScan));
        return "results";
    }
    
    @GetMapping("/scans")
    public String showAllScans(Model model) {
        model.addAttribute("scans", scanService.getAllScans());
        model.addAttribute("activePage", "scans");
        return "scans";
    }

   @GetMapping("/scans/{id}/edit")
    public String showEditScanForm(@PathVariable Long id, Model model) {
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
        if (auditName == null || auditName.isBlank()) {
            model.addAttribute("errorMessage", "Scan name cannot be empty.");
            return showEditScanForm(id, model);
        }
        scanService.updateScanName(id, auditName);
        return "redirect:/scans";
    }

    @PostMapping("/scans/{id}/delete")
    public String deleteScan(@PathVariable Long id) {
        scanService.deleteScanById(id);
        return "redirect:/scans";
    }
    
    @GetMapping("/results/{id}")
    public String showScanById(@PathVariable Long id, Model model) {
        Scan scan = scanService.getScanById(id);
        if (scan == null) {
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
        try {
            String normalizedUrl = normalizeUrl(url);
            validateUrl(normalizedUrl);

            Path jsonPath = scannerProcessService.runScan(normalizedUrl);
            scanService.processScannedJson(jsonPath, auditName);

            return "redirect:/results/latest";

        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("activePage", "scan");
            return "new-scan";

        } catch (IOException | InterruptedException e) {
            model.addAttribute("errorMessage", "Unable to scan the page.");
            model.addAttribute("activePage", "scan");
            return "new-scan";
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

        Scan scan = scanService.getScanById(id);

        if (scan != null) {
            AiProvider provider = AiProvider.GEMINI;
            AiTier tier = AiTier.FREE;

            switch (aiChoice) {
                case "GEMINI_PAID" -> {
                    provider = AiProvider.GEMINI;
                    tier = AiTier.PAID;
                }
                case "OPENAI_PAID" -> {
                    provider = AiProvider.OPEN_AI;
                    tier = AiTier.PAID;
                }
                default -> {
                    provider = AiProvider.GEMINI;
                    tier = AiTier.FREE;
                }
            }

            remediationService.generateRemediationsForScan(scan, provider, tier);
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
    
