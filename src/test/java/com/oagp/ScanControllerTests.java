package com.oagp;

import com.oagp.service.ScanService;
import com.oagp.service.ScannerProcessService;
import com.oagp.service.RemediationService;
import com.oagp.repository.ScanReportRepository;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

@SpringBootTest
@AutoConfigureMockMvc
class ScanControllerTests {

    @Autowired
    private MockMvc mockMvc;

    // Mock service responsible for scan persistence and retrieval
    @MockBean
    private ScanService scanService;

    // Mock service responsible for executing the scanning process
    @MockBean
    private ScannerProcessService scannerProcessService;

    // Mock service responsible for AI remediation logic
    @MockBean
    private RemediationService remediationService;

    // Mock repository used by the controller to retrieve scan reports
    @MockBean
    private ScanReportRepository scanReportRepository;

    @Test
    @DisplayName("Should load homepage successfully")
    void shouldLoadHomePage() throws Exception {

        System.out.println(">>> Running homepage load test");

        // Act & Assert: verify that the root endpoint returns HTTP 200
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should execute scan and redirect to latest scan view")
    void shouldRunScanSuccessfully() throws Exception {

        System.out.println(">>> Running successful scan test");

        // Arrange: simulate successful scan execution returning a JSON file
        Path fakePath = Paths.get("results.json");

        when(scannerProcessService.runScan(anyString()))
                .thenReturn(fakePath);

        // Act & Assert: perform scan request and verify redirect behavior
        mockMvc.perform(post("/scan")
                .param("url", "https://google.com")
                .param("auditName", "Google Scan Test"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/?recentScan=true"));
    }
}