package com.oagp.service;

import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ScannerProcessService {

    private static final Logger log = LoggerFactory.getLogger(ScannerProcessService.class);

    public Path runScan(String url) throws IOException, InterruptedException {
        log.info("Starting scan for URL: {}", url);
        Path projectRoot = Paths.get("").toAbsolutePath();
        Path scannerDir = projectRoot.resolve("scanner");
        Path outputPath = projectRoot.resolve("results.json");
        
        log.debug("Scanner directory: {}", scannerDir);
        log.debug("Output path: {}", outputPath);
        
        archiveExistingResults(outputPath, projectRoot);

        ProcessBuilder processBuilder = new ProcessBuilder(
                "node",
                "scan-page.js",
                url,
                outputPath.toString()
        );

        processBuilder.directory(scannerDir.toFile());
        processBuilder.redirectErrorStream(true);

        log.debug("Starting Node.js process: node scan-page.js {} {}", url, outputPath);
        Process process = processBuilder.start();

        StringBuilder output = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append(System.lineSeparator());
                log.trace("Scanner output: {}", line);
            }
        }

        int exitCode = process.waitFor();
        log.debug("Scanner process completed with exit code: {}", exitCode);

        if (exitCode != 0) {
            log.error("Scanner process failed with exit code: {}, output: {}", exitCode, output);
            throw new IOException("Scanner process failed:\n" + output);
        }

        log.info("Successfully completed scan for URL: {}, results saved to: {}", url, outputPath);
        return outputPath;
    }
    
    private void archiveExistingResults(Path outputPath, Path projectRoot) throws IOException {
        if (!Files.exists(outputPath)) {
            log.debug("No existing results file to archive");
            return;
        }

        log.debug("Archiving existing results file: {}", outputPath);
        Path archiveDir = projectRoot.resolve("scan-archive");
        Files.createDirectories(archiveDir);

        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));

        Path archivedFile = archiveDir.resolve("results_" + timestamp + ".json");

        Files.copy(outputPath, archivedFile, StandardCopyOption.REPLACE_EXISTING);
        log.info("Archived previous results to: {}", archivedFile);
    }
}