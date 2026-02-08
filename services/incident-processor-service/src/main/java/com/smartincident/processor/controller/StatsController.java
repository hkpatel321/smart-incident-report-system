package com.smartincident.processor.controller;

import com.smartincident.processor.entity.Severity;
import com.smartincident.processor.entity.Status;
import com.smartincident.processor.repository.IncidentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for incident statistics and monitoring.
 */
@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final IncidentRepository incidentRepository;

    /**
     * Get incident statistics for dashboard.
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();

        // Total count
        stats.put("totalIncidents", incidentRepository.count());

        // By status
        Map<String, Long> byStatus = new HashMap<>();
        for (Status status : Status.values()) {
            byStatus.put(status.name(), incidentRepository.countByStatus(status));
        }
        stats.put("byStatus", byStatus);

        // By severity
        Map<String, Long> bySeverity = new HashMap<>();
        for (Severity severity : Severity.values()) {
            bySeverity.put(severity.name(), incidentRepository.countBySeverity(severity));
        }
        stats.put("bySeverity", bySeverity);

        // Urgent unprocessed
        stats.put("urgentUnprocessed", incidentRepository.findUrgentUnprocessedIncidents().size());

        return ResponseEntity.ok(stats);
    }

    /**
     * Health check endpoint.
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Incident Processor Service is running");
    }
}
