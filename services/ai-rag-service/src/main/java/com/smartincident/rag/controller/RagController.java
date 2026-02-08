package com.smartincident.rag.controller;

import com.smartincident.rag.dto.ResolutionRequest;
import com.smartincident.rag.dto.ResolutionResponse;
import com.smartincident.rag.service.RagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for AI-assisted incident resolution.
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Slf4j
public class RagController {

    private final RagService ragService;

    /**
     * Generate resolution recommendation for an incident.
     */
    @PostMapping("/resolve")
    public ResponseEntity<ResolutionResponse> getResolution(
            @Valid @RequestBody ResolutionRequest request) {
        log.info("Resolution request for incident: {}", request.getIncidentId());
        ResolutionResponse response = ragService.generateResolution(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Health check.
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("AI RAG Service is running");
    }
}
