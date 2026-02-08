package com.smartincident.ingest.service;

import com.smartincident.ingest.config.KafkaProducerConfig;
import com.smartincident.ingest.dto.IncidentRequest;
import com.smartincident.ingest.dto.IncidentResponse;
import com.smartincident.ingest.event.IncidentCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Service responsible for processing incident submissions
 * and publishing events to Kafka.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IncidentService {

    private final KafkaTemplate<String, IncidentCreatedEvent> kafkaTemplate;

    private static final String STATUS_NEW = "NEW";

    /**
     * Processes an incident request and publishes it to Kafka.
     *
     * @param request the incident request
     * @return response with incident details
     */
    public IncidentResponse submitIncident(IncidentRequest request) {
        String incidentId = generateIncidentId();
        Instant now = Instant.now();

        // Build Kafka event
        IncidentCreatedEvent event = buildEvent(incidentId, request, now);

        // Publish to Kafka asynchronously
        publishToKafka(incidentId, event);

        // Return response
        return IncidentResponse.builder()
                .incidentId(incidentId)
                .title(request.getTitle())
                .severity(request.getSeverity())
                .status(STATUS_NEW)
                .createdAt(now)
                .message("Incident submitted successfully")
                .build();
    }

    private IncidentCreatedEvent buildEvent(String incidentId, IncidentRequest request, Instant timestamp) {
        IncidentCreatedEvent.IncidentPayload payload = IncidentCreatedEvent.IncidentPayload.builder()
                .id(incidentId)
                .title(request.getTitle())
                .description(request.getDescription())
                .source(request.getSource())
                .category(request.getCategory())
                .severity(request.getSeverity())
                .status(STATUS_NEW)
                .reporterEmail(request.getReporterEmail())
                .createdAt(timestamp)
                .metadata(request.getMetadata())
                .build();

        return IncidentCreatedEvent.builder()
                .eventId("evt-" + UUID.randomUUID())
                .eventType("INCIDENT_CREATED")
                .timestamp(timestamp)
                .payload(payload)
                .build();
    }

    private void publishToKafka(String incidentId, IncidentCreatedEvent event) {
        CompletableFuture<SendResult<String, IncidentCreatedEvent>> future = kafkaTemplate.send(
                KafkaProducerConfig.INCIDENT_CREATED_TOPIC,
                incidentId,
                event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish incident {} to Kafka: {}", incidentId, ex.getMessage());
            } else {
                log.info("Incident {} published to Kafka. Partition: {}, Offset: {}",
                        incidentId,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }

    private String generateIncidentId() {
        return "INC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
