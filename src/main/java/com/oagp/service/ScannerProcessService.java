package com.oagp.service;

import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ScannerProcessService {

    public Path runScan(String url) throws IOException, InterruptedException {
        Path projectRoot = Paths.get("").toAbsolutePath();
        Path scannerDir = projectRoot.resolve("scanner");
        Path outputPath = projectRoot.resolve("results.json");

        ProcessBuilder processBuilder = new ProcessBuilder(
                "node",
                "scan-page.js",
                url,
                outputPath.toString()
        );

        processBuilder.directory(scannerDir.toFile());
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        StringBuilder output = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append(System.lineSeparator());
            }
        }

        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new IOException("Scanner process failed:\n" + output);
        }

        return outputPath;
    }
}