package com.oagp;

import com.oagp.service.ScanService;
import com.oagp.service.ScannerProcessService;
import com.oagp.service.RemediationService;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyString;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

@SpringBootTest
@AutoConfigureMockMvc
class UrlValidationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ScanService scanService;

    @MockBean
    private ScannerProcessService scannerProcessService;

    @MockBean
    private RemediationService remediationService;

    // Test blank url
    @Test
    void shouldRejectBlankUrl() throws Exception {

        System.out.println("Running blank url test");

        mockMvc.perform(post("/scan")
                .param("url", "")
                .param("auditName", "Test"))
                .andExpect(status().isOk())
                .andExpect(view().name("new-scan"))
                .andExpect(model().attributeExists("errorMessage"));
    }

    // Test invalid url
    @Test
    void shouldRejectInvalidUrl() throws Exception {

        System.out.println("Running invalid url test");

        mockMvc.perform(post("/scan")
                .param("url", "%%%invalid%%%")
                .param("auditName", "Test"))
                .andExpect(status().isOk())
                .andExpect(view().name("new-scan"))
                .andExpect(model().attributeExists("errorMessage"));
    }

    // Test invalid protocol
    @Test
    void shouldRejectFtpProtocol() throws Exception {

        System.out.println("Running ftp protocol test");

        mockMvc.perform(post("/scan")
                .param("url", "ftp://google.com")
                .param("auditName", "Test"))
                .andExpect(status().isOk())
                .andExpect(view().name("new-scan"))
                .andExpect(model().attributeExists("errorMessage"));
    }

    // Test normal url
    @Test
    void shouldAcceptUrlWithoutHttp() throws Exception {

        System.out.println("Running normal url test");

        Path path = Paths.get("results.json");

        when(scannerProcessService.runScan(anyString()))
                .thenReturn(path);

        mockMvc.perform(post("/scan")
                .param("url", "google.com")
                .param("auditName", "Google"))
                .andExpect(status().is3xxRedirection());
    }

    // Test localhost
    @Test
    void shouldAcceptLocalhost() throws Exception {

        System.out.println("Running localhost test");

        Path path = Paths.get("results.json");

        when(scannerProcessService.runScan(anyString()))
                .thenReturn(path);

        mockMvc.perform(post("/scan")
                .param("url", "localhost:8080")
                .param("auditName", "Local"))
                .andExpect(status().is3xxRedirection());
    }
}