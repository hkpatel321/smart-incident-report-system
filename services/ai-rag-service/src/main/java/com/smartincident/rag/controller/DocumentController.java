package com.smartincident.rag.controller;

import com.smartincident.rag.client.GeminiClient;
import com.smartincident.rag.dto.DocumentRequest;
import com.smartincident.rag.entity.KnowledgeDocument;
import com.smartincident.rag.repository.KnowledgeDocumentRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller for managing knowledge base documents.
 */
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Slf4j
public class DocumentController {

    private final KnowledgeDocumentRepository documentRepository;
    private final GeminiClient geminiClient;

    /**
     * Ingest a new document into the knowledge base.
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> ingestDocument(
            @Valid @RequestBody DocumentRequest request) {
        log.info("Ingesting document: {}", request.getTitle());

        // Generate embedding
        float[] embedding = geminiClient.generateEmbedding(
                request.getTitle() + ". " + request.getContent());

        // Save document
        KnowledgeDocument doc = KnowledgeDocument.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .docType(request.getDocType())
                .sourceId(request.getSourceId())
                .tags(request.getTags())
                .embedding(GeminiClient.embeddingToString(embedding))
                .build();

        Long savedId = documentRepository.saveNative(
                doc.getTitle(),
                doc.getContent(),
                doc.getDocType(),
                doc.getSourceId(),
                doc.getTags(),
                doc.getEmbedding());
        log.info("Document ingested with ID: {}", savedId);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", savedId,
                "title", doc.getTitle(),
                "message", "Document ingested successfully"));
    }

    /**
     * List all documents.
     */
    @GetMapping
    public ResponseEntity<List<KnowledgeDocument>> listDocuments() {
        return ResponseEntity.ok(documentRepository.findAll());
    }

    /**
     * Get document by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<KnowledgeDocument> getDocument(@PathVariable Long id) {
        return documentRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Delete document.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        if (!documentRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        documentRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
