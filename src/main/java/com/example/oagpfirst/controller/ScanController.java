package com.example.oagpfirst.controller;

import com.example.oagpfirst.model.Scan;
import com.example.oagpfirst.service.ScanService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ScanController {

    private final ScanService scanService;

    public ScanController(ScanService scanService) {
        this.scanService = scanService;
    }

    @GetMapping("/")
    public String showLatestScan(Model model) {
        // Call the service layer to retrieve the most recent scan from the database
        Scan latestScan = scanService.getLatestScan();
        // Add the scan object to the model
        // This makes it accessible in the Thymeleaf HTML file (output.html)
        model.addAttribute("scan", latestScan);
        return "output";
    }
}