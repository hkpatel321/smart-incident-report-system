package com.smartincident.processor.service;

import com.smartincident.processor.config.KafkaConsumerConfig;
import com.smartincident.processor.entity.Category;
import com.smartincident.processor.entity.Incident;
import com.smartincident.processor.entity.Severity;
import com.smartincident.processor.entity.Status;
import com.smartincident.processor.event.IncidentCreatedEvent;
import com.smartincident.processor.repository.IncidentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Service that processes incoming incidents from Kafka.
 * 
 * Responsibilities:
 * - Consume incidents from Kafka
 * - Classify severity using rule-based logic
 * - Persist to PostgreSQL
 * - Handle failures gracefully
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IncidentProcessingService {

    private final IncidentRepository incidentRepository;
    private final ClassificationService classificationService;

    /**
     * Kafka listener for incident-created events.
     * Processes and persists incidents with classification.
     */
    @KafkaListener(topics = KafkaConsumerConfig.INCIDENT_CREATED_TOPIC, groupId = "${spring.kafka.consumer.group-id}", containerFactory = "kafkaListenerContainerFactory")
    @Transactional
    public void processIncident(IncidentCreatedEvent event) {
        String incidentId = event.getPayload().getId();
        log.info("Processing incident: {} - {}", incidentId, event.getPayload().getTitle());

        try {
            // Check for duplicate
            if (incidentRepository.existsById(incidentId)) {
                log.warn("Incident {} already exists, skipping", incidentId);
                return;
            }

            // Extract payload
            IncidentCreatedEvent.IncidentPayload payload = event.getPayload();

            // Parse enums safely
            Category category = parseCategory(payload.getCategory());
            Severity originalSeverity = parseSeverity(payload.getSeverity());

            // Build processing logs
            StringBuilder logBuilder = new StringBuilder();
            logBuilder.append(String.format("[%s] Incident %s received from %s%n",
                    Instant.now(), incidentId, payload.getSource()));
            logBuilder.append(String.format("[%s] Category parsed: %s%n",
                    Instant.now(), category));
            logBuilder.append(String.format("[%s] Original severity: %s%n",
                    Instant.now(), originalSeverity));

            // Classify severity
            ClassificationService.ClassificationResult classification = classificationService.classify(
                    payload.getTitle(),
                    payload.getDescription(),
                    category,
                    originalSeverity,
                    payload.getMetadata());

            logBuilder.append(String.format("[%s] Classification complete — severity: %s (confidence: %.2f)%n",
                    Instant.now(), classification.severity(), classification.confidence()));
            if (classification.severity() != originalSeverity) {
                logBuilder.append(String.format("[%s] ESCALATION: %s → %s%n",
                        Instant.now(), originalSeverity, classification.severity()));
            }
            logBuilder.append(String.format("[%s] Incident persisted with status PROCESSING%n", Instant.now()));

            // Build and save entity
            Incident incident = Incident.builder()
                    .id(incidentId)
                    .title(payload.getTitle())
                    .description(payload.getDescription())
                    .source(payload.getSource())
                    .category(category)
                    .originalSeverity(originalSeverity)
                    .classifiedSeverity(classification.severity())
                    .status(Status.PROCESSING)
                    .reporterEmail(payload.getReporterEmail())
                    .classificationConfidence(classification.confidence())
                    .suggestedCategory(classification.suggestedCategory())
                    .logs(logBuilder.toString())
                    .metadata(payload.getMetadata())
                    .createdAt(payload.getCreatedAt())
                    .processedAt(Instant.now())
                    .build();

            incidentRepository.save(incident);

            log.info("Incident {} processed successfully. Original severity: {}, Classified: {} (confidence: {})",
                    incidentId,
                    originalSeverity,
                    classification.severity(),
                    String.format("%.2f", classification.confidence()));

            // Log escalation if severity changed
            if (classification.severity() != originalSeverity) {
                log.warn("ESCALATION: Incident {} escalated from {} to {}",
                        incidentId, originalSeverity, classification.severity());
            }

        } catch (Exception e) {
            log.error("Failed to process incident {}: {}", incidentId, e.getMessage(), e);
            throw e; // Re-throw to trigger Kafka retry
        }
    }

    private Category parseCategory(String category) {
        try {
            return Category.valueOf(category.toUpperCase());
        } catch (Exception e) {
            log.warn("Unknown category: {}, defaulting to OTHER", category);
            return Category.OTHER;
        }
    }

    private Severity parseSeverity(String severity) {
        try {
            return Severity.valueOf(severity.toUpperCase());
        } catch (Exception e) {
            log.warn("Unknown severity: {}, defaulting to MEDIUM", severity);
            return Severity.MEDIUM;
        }
    }
}
