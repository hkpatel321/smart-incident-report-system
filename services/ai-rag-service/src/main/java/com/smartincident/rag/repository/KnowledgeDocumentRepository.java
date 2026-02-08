package com.smartincident.rag.repository;

import com.smartincident.rag.entity.DocumentType;
import com.smartincident.rag.entity.KnowledgeDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for knowledge documents with vector similarity search.
 */
@Repository
public interface KnowledgeDocumentRepository extends JpaRepository<KnowledgeDocument, Long> {

    List<KnowledgeDocument> findByDocType(DocumentType docType);

    /**
     * Find similar documents using cosine similarity.
     * pgvector's <=> operator computes cosine distance (1 - similarity).
     *
     * @param embedding Query embedding
     * @param limit     Max results
     * @return Similar documents ordered by similarity (highest first)
     */
    @Query(value = """
            SELECT *, 1 - (embedding <=> CAST(:embedding AS vector)) AS similarity
            FROM ai_rag.knowledge_documents
            WHERE embedding IS NOT NULL
            ORDER BY embedding <=> CAST(:embedding AS vector)
            LIMIT :limit
            """, nativeQuery = true)
    List<KnowledgeDocument> findSimilarDocuments(
            @Param("embedding") String embedding,
            @Param("limit") int limit);

    /**
     * Find similar documents with minimum similarity threshold.
     */
    @Query(value = """
            SELECT *, 1 - (embedding <=> CAST(:embedding AS vector)) AS similarity
            FROM ai_rag.knowledge_documents
            WHERE embedding IS NOT NULL
              AND 1 - (embedding <=> CAST(:embedding AS vector)) >= :minSimilarity
            ORDER BY embedding <=> CAST(:embedding AS vector)
            LIMIT :limit
            """, nativeQuery = true)
    List<KnowledgeDocument> findSimilarDocumentsWithThreshold(
            @Param("embedding") String embedding,
            @Param("minSimilarity") double minSimilarity,
            @Param("limit") int limit);

    /**
     * Find similar documents of a specific type.
     */
    @Query(value = """
            SELECT *, 1 - (embedding <=> CAST(:embedding AS vector)) AS similarity
            FROM ai_rag.knowledge_documents
            WHERE embedding IS NOT NULL
              AND doc_type = :docType
            ORDER BY embedding <=> CAST(:embedding AS vector)
            LIMIT :limit
            """, nativeQuery = true)
    List<KnowledgeDocument> findSimilarByType(
            @Param("embedding") String embedding,
            @Param("docType") String docType,
            @Param("limit") int limit);

    boolean existsBySourceId(String sourceId);
}
