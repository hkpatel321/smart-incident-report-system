package com.smartincident.notification.controller;

import com.smartincident.notification.entity.NotificationLog;
import com.smartincident.notification.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller exposing notification log data and health endpoint.
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationLogRepository logRepository;

    /**
     * Health check endpoint.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "notification-service"));
    }

    /**
     * List all notification log entries (most recent first).
     */
    @GetMapping
    public ResponseEntity<List<NotificationLog>> getAllNotifications() {
        List<NotificationLog> logs = logRepository.findAllByOrderByCreatedAtDesc();
        log.debug("Returning {} notification log entries", logs.size());
        return ResponseEntity.ok(logs);
    }

    /**
     * List notification logs for a specific incident.
     */
    @GetMapping("/incident/{incidentId}")
    public ResponseEntity<List<NotificationLog>> getNotificationsForIncident(
            @PathVariable String incidentId) {
        List<NotificationLog> logs = logRepository.findByIncidentIdOrderByCreatedAtDesc(incidentId);
        return ResponseEntity.ok(logs);
    }
}
