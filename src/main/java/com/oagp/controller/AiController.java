package com.oagp.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.oagp.model.AiProvider;
import com.oagp.model.AiTier;
import com.oagp.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai")
public class AiController {

    private static final Logger log =
            LoggerFactory.getLogger(AiController.class);

    private final AiService aiServices;

    @Autowired
    public AiController(AiService aiServices) {
        this.aiServices = aiServices;
    }

    @GetMapping("/{provider}/{tier}/{prompt}")
    public String ask(@PathVariable AiProvider provider,
                      @PathVariable AiTier tier,
                      @PathVariable String prompt) {
        log.info("AiController asking for provider {} and tier {} and prompt {}", provider, tier, prompt);
        return aiServices.generateRemediation(prompt, provider, tier);
    }

    @GetMapping("/{prompt}")
    public String ask(@PathVariable String prompt) {
        log.info("AiController asking for prompt {}", prompt);
        return aiServices.generateRemediation(prompt);
    }
}
