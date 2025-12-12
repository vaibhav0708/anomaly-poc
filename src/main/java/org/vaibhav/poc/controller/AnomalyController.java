package org.vaibhav.poc.controller;

import org.springframework.web.bind.annotation.*;
import org.vaibhav.poc.model.AnomalyResult;
import org.vaibhav.poc.service.LLMService;

import java.util.List;

@RestController
@RequestMapping("/api/anomaly")
public class AnomalyController {

    private final LLMService llmService;

    public AnomalyController(LLMService llmService) {
        this.llmService = llmService;
    }

    @PostMapping("/detect")
    public AnomalyResult detect(@RequestBody EventRequest req) {
        return llmService.detectAnomaly(req.newEvent(), req.similarEvents());
    }

    public record EventRequest(String newEvent, List<String> similarEvents) {}
}
