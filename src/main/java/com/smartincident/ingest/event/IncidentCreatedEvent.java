package com.smartincident.ingest.event;

import com.smartincident.ingest.dto.Category;
import com.smartincident.ingest.dto.Severity;
import com.smartincident.ingest.dto.Source;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * Kafka event published when a new incident is created.
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
        private Source source;
        private Category category;
        private Severity severity;
        private String status;
        private String reporterEmail;
        private Instant createdAt;
        private Map<String, Object> metadata;
    }
}
