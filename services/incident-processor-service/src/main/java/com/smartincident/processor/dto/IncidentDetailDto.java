package com.smartincident.processor.dto;

import com.smartincident.processor.entity.Category;
import com.smartincident.processor.entity.Incident;
import com.smartincident.processor.entity.Severity;
import com.smartincident.processor.entity.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * DTO for detailed incident view.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentDetailDto {

    private String id;
    private String title;
    private String description;
    private String source;
    private Category category;
    private Severity originalSeverity;
    private Severity classifiedSeverity;
    private Status status;
    private String reporterEmail;
    private String assignedTo;
    private String aiRecommendation;
    private String logs;
    private Double classificationConfidence;
    private String suggestedCategory;
    private Map<String, Object> metadata;
    private Instant createdAt;
    private Instant processedAt;
    private Instant resolvedAt;
    private Instant updatedAt;

    /**
     * Convert entity to detailed DTO.
     */
    public static IncidentDetailDto fromEntity(Incident incident) {
        return IncidentDetailDto.builder()
                .id(incident.getId())
                .title(incident.getTitle())
                .description(incident.getDescription())
                .source(incident.getSource())
                .category(incident.getCategory())
                .originalSeverity(incident.getOriginalSeverity())
                .classifiedSeverity(incident.getClassifiedSeverity())
                .status(incident.getStatus())
                .reporterEmail(incident.getReporterEmail())
                .assignedTo(incident.getAssignedTo())
                .aiRecommendation(incident.getAiRecommendation())
                .logs(incident.getLogs())
                .classificationConfidence(incident.getClassificationConfidence())
                .suggestedCategory(incident.getSuggestedCategory())
                .metadata(incident.getMetadata())
                .createdAt(incident.getCreatedAt())
                .processedAt(incident.getProcessedAt())
                .resolvedAt(incident.getResolvedAt())
                .updatedAt(incident.getUpdatedAt())
                .build();
    }
}
