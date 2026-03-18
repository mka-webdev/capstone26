package com.example.oagpfirst.config;
// Import the service layer that handles processing of scan data
import com.example.oagpfirst.service.ScanService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class StartupDataLoader implements CommandLineRunner {
    // Reference to the ScanService (business logic layer)
    private final ScanService scanService;

    public StartupDataLoader(ScanService scanService) {
        this.scanService = scanService;
    }

    @Override
    public void run(String... args) throws Exception {
        // Define the path to the JSON file (results.json) in the project root directory
        Path jsonPath = Paths.get("results.json");
        // Check if the file exists before trying to process it
        if (!Files.exists(jsonPath)) {
            System.out.println("results.json not found in project folder.");
            return;
        }
        // - Reading the JSON
        // - Extracting scan metadata (URL, engine, timestamp, etc.)
        // - Extracting violations (id, impact, description, etc.)
        // - Saving everything to the database (SQLite)
        scanService.processJsonFile(jsonPath);
        System.out.println("results.json imported successfully.");
    }
}