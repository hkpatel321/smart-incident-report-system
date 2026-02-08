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

/**
 * DTO for incident list responses (summary view).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentSummaryDto {

    private String id;
    private String title;
    private String source;
    private Category category;
    private Severity originalSeverity;
    private Severity classifiedSeverity;
    private Status status;
    private String reporterEmail;
    private Double classificationConfidence;
    private Instant createdAt;
    private Instant processedAt;

    /**
     * Convert entity to DTO.
     */
    public static IncidentSummaryDto fromEntity(Incident incident) {
        return IncidentSummaryDto.builder()
                .id(incident.getId())
                .title(incident.getTitle())
                .source(incident.getSource())
                .category(incident.getCategory())
                .originalSeverity(incident.getOriginalSeverity())
                .classifiedSeverity(incident.getClassifiedSeverity())
                .status(incident.getStatus())
                .reporterEmail(incident.getReporterEmail())
                .classificationConfidence(incident.getClassificationConfidence())
                .createdAt(incident.getCreatedAt())
                .processedAt(incident.getProcessedAt())
                .build();
    }
}
