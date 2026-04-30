package com.oagp;

import com.oagp.model.Scan;
import com.oagp.model.Violation;
import com.oagp.model.ScanReport;
import com.oagp.repository.ScanReportRepository;
import com.oagp.service.AiService;
import com.oagp.service.PromptBuilderService;
import com.oagp.service.RemediationService;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

class RemediationServiceTests {

    // Mock dependency responsible for building AI prompts
    @Mock
    private PromptBuilderService promptBuilderService;

    // Mock dependency representing the AI provider service
    @Mock
    private AiService aiService;

    // Mock repository used to persist generated scan reports
    @Mock
    private ScanReportRepository scanReportRepository;

    // Class under test with injected mock dependencies
    @InjectMocks
    private RemediationService remediationService;

    // Initialize Mockito annotations before running tests
    public RemediationServiceTests() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldGenerateRemediationForScan() {

        System.out.println(">>> Running remediation logic test");

        // Arrange: create a minimal Scan object with one violation
        Scan scan = new Scan();
        Violation v1 = new Violation();
        scan.setViolations(List.of(v1));

        // Mock prompt generation for the entire scan
        when(promptBuilderService.buildPromptForWholeScan(any()))
                .thenReturn("test prompt");

        // Mock AI response for remediation generation
        when(aiService.generateRemediation(anyString()))
                .thenReturn("generated remediation");

        // Mock repository lookup to simulate no existing report for the scan
        when(scanReportRepository.findByScanId(any()))
                .thenReturn(Optional.empty());

        // Act: execute remediation generation logic
        remediationService.generateRemediationsForScan(scan);

        // Assert: verify that a new ScanReport is persisted
        verify(scanReportRepository, times(1))
                .save(any(ScanReport.class));
    }
}