package com.oagp;

import com.oagp.model.Scan;
import com.oagp.model.Violation;
import com.oagp.model.AiProvider;
import com.oagp.model.AiTier;
import com.oagp.repository.ViolationRepository;
import com.oagp.service.AiService;
import com.oagp.service.PromptBuilderService;
import com.oagp.service.RemediationService;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.mockito.Mockito.*;

class RemediationServiceTests {

    @Mock
    private PromptBuilderService promptBuilderService;

    @Mock
    private AiService aiService;

    @Mock
    private ViolationRepository violationRepository;

    @InjectMocks
    private RemediationService remediationService;

    public RemediationServiceTests() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldGenerateRemediationForScan() {

        System.out.println(">>> Running remediation service test");

        // fake data
        Scan scan = new Scan();
        Violation v1 = new Violation();
        scan.setViolations(List.of(v1));

        when(promptBuilderService.buildPromptForWholeScan(scan))
                .thenReturn("test prompt");

        when(aiService.generateRemediation(anyString(), any(), any()))
                .thenReturn("AI response");

        // 🔥 FIX AQUÍ
        remediationService.generateRemediationsForScan(
                scan,
                AiProvider.GEMINI,
                AiTier.FREE
        );

        verify(violationRepository, times(1)).save(any(Violation.class));
    }
}