package com.smartincident.ingest.controller;

import com.smartincident.ingest.dto.IncidentRequest;
import com.smartincident.ingest.dto.IncidentResponse;
import com.smartincident.ingest.service.IncidentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for incident ingestion endpoints.
 */
@RestController
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
@Slf4j
public class IncidentController {

    private final IncidentService incidentService;

    /**
     * Submit a new incident.
     *
     * @param request the incident request (validated)
     * @return the created incident response
     */
    @PostMapping
    public ResponseEntity<IncidentResponse> submitIncident(@Valid @RequestBody IncidentRequest request) {
        log.info("Received incident submission: {}", request.getTitle());
        IncidentResponse response = incidentService.submitIncident(request);
        log.info("Incident created with ID: {}", response.getIncidentId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Health check endpoint.
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Incident Ingest Service is running");
    }
}
