package com.smartincident.rag.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Knowledge document with vector embedding for similarity search.
 */
@Entity
@Table(name = "knowledge_documents", schema = "ai_rag")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "doc_type", nullable = false, length = 30)
    private DocumentType docType;

    @Column(name = "source_id", length = 100)
    private String sourceId; // e.g., incident ID if from past incident

    @Column(name = "tags", length = 500)
    private String tags; // comma-separated tags

    /**
     * Vector embedding (768 dimensions).
     * Stored as String (casted to vector by DB) to avoid Hibernate mapping issues.
     */
    @Column(name = "embedding", columnDefinition = "vector(768)")
    private String embedding;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
