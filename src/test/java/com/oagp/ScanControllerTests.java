package com.oagp;

import com.oagp.service.ScanService;
import com.oagp.service.ScannerProcessService;
import com.oagp.service.RemediationService;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
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

    @MockBean
    private ScanService scanService;

    @MockBean
    private ScannerProcessService scannerProcessService;

    @MockBean
    private RemediationService remediationService;

    // This test verifies homepage loads correctly
    @Test
    void shouldLoadHomePage() throws Exception {

        System.out.println(">>> Running homepage load test");

        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }

    // This test verifies successful scan execution
    @Test
    void shouldRunScanSuccessfully() throws Exception {

        System.out.println(">>> Running successful scan test");

        Path fakePath = Paths.get("results.json");

        when(scannerProcessService.runScan(anyString()))
                .thenReturn(fakePath);

        mockMvc.perform(post("/scan")
                .param("url", "https://google.com")
                .param("auditName", "Google Scan Test"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/results/latest"));
    }
}