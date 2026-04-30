package com.oagp;

import com.oagp.service.AiService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AiControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AiService aiService;

    @Test
    void shouldGenerateAiResponse() throws Exception {

        System.out.println(">>> Running AI controller test");

        // This test verifies that the AI endpoint
        // returns a successful response

        when(aiService.generateRemediation(anyString()))
                .thenReturn("AI remediation generated");

        mockMvc.perform(get("/ai/testPrompt"))
                .andExpect(status().isOk());
    }
}