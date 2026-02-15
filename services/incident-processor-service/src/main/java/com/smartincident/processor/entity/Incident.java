package com.smartincident.processor.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;

/**
 * JPA Entity representing a processed incident.
 */
@Entity
@Table(name = "incidents", schema = "processor")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Incident {

    @Id
    @Column(name = "id", length = 50)
    private String id;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "source", nullable = false, length = 50)
    private String source;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(name = "original_severity", nullable = false, length = 20)
    private Severity originalSeverity;

    @Enumerated(EnumType.STRING)
    @Column(name = "classified_severity", nullable = false, length = 20)
    private Severity classifiedSeverity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Status status;

    @Column(name = "reporter_email", length = 100)
    private String reporterEmail;

    @Column(name = "assigned_to", length = 100)
    private String assignedTo;

    @Column(name = "ai_recommendation", columnDefinition = "TEXT")
    private String aiRecommendation;

    @Column(name = "logs", columnDefinition = "TEXT")
    private String logs;

    @Column(name = "classification_confidence")
    private Double classificationConfidence;

    @Column(name = "suggested_category", length = 100)
    private String suggestedCategory;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "processed_at")
    private Instant processedAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        if (processedAt == null) {
            processedAt = Instant.now();
        }
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
