package com.oagp.controller;



import com.oagp.model.AIProvider;
import com.oagp.model.AITier;
import com.oagp.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai")
public class AIController {

    private final AiService aiServices;

    @Autowired
    public AIController(AiService aiServices) {
        this.aiServices = aiServices;
    }

    @GetMapping("/{provider}/{tier}/{prompt}")
    public String ask(@PathVariable AIProvider provider,
                      @PathVariable AITier tier,
                      @PathVariable String prompt) {
        return aiServices.generateRemediation(prompt, provider, tier);
    }
}
