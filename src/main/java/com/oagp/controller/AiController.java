package com.oagp.controller;



import com.oagp.model.AiProvider;
import com.oagp.model.AiTier;
import com.oagp.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai")
public class AiController {

    private final AiService aiServices;

    @Autowired
    public AiController(AiService aiServices) {
        this.aiServices = aiServices;
    }

    @GetMapping("/{provider}/{tier}/{prompt}")
    public String ask(@PathVariable AiProvider provider,
                      @PathVariable AiTier tier,
                      @PathVariable String prompt) {
        return aiServices.generateRemediation(prompt, provider, tier);
    }
}
