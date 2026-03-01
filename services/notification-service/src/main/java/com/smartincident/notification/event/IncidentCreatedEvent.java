package com.smartincident.notification.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * Kafka event consumed from the incident-created topic.
 * Mirror of the processor-service event structure.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentCreatedEvent {

    private String eventId;
    private String eventType;
    private Instant timestamp;
    private IncidentPayload payload;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IncidentPayload {
        private String id;
        private String title;
        private String description;
        private String source;
        private String category;
        private String severity;
        private String status;
        private String reporterEmail;
        private Instant createdAt;
        private Map<String, Object> metadata;
    }
}
