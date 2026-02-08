package com.smartincident.rag.service;

import com.smartincident.rag.client.GeminiClient;
import com.smartincident.rag.dto.ResolutionRequest;
import com.smartincident.rag.dto.ResolutionResponse;
import com.smartincident.rag.entity.KnowledgeDocument;
import com.smartincident.rag.repository.KnowledgeDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * RAG Service for incident resolution recommendations.
 * 
 * Pipeline:
 * 1. Embed incident description
 * 2. Retrieve similar documents from knowledge base
 * 3. Generate resolution using context + Gemini
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RagService {

    private final GeminiClient geminiClient;
    private final KnowledgeDocumentRepository documentRepository;

    @Value("${rag.retrieval.top-k:5}")
    private int topK;

    @Value("${rag.retrieval.min-similarity:0.5}")
    private double minSimilarity;

    /**
     * Generate resolution recommendation for an incident.
     */
    public ResolutionResponse generateResolution(ResolutionRequest request) {
        log.info("Generating resolution for incident: {}", request.getIncidentId());

        // Step 1: Create query text
        String queryText = String.format("%s. %s", request.getTitle(), request.getDescription());

        // Step 2: Generate embedding for the query
        float[] queryEmbedding = geminiClient.generateEmbedding(queryText);
        String embeddingStr = GeminiClient.embeddingToString(queryEmbedding);

        // Step 3: Retrieve similar documents
        List<KnowledgeDocument> relevantDocs = documentRepository.findSimilarDocumentsWithThreshold(
                embeddingStr, minSimilarity, topK);

        log.info("Found {} relevant documents for incident {}",
                relevantDocs.size(), request.getIncidentId());

        // Step 4: Build context from retrieved documents
        String context = buildContext(relevantDocs);

        // Step 5: Generate resolution using Gemini
        String prompt = buildPrompt(request, context);
        String rawResponse = geminiClient.generateContent(prompt);

        // Step 6: Parse and structure the response
        return parseResponse(request.getIncidentId(), rawResponse, relevantDocs);
    }

    private String buildContext(List<KnowledgeDocument> documents) {
        if (documents.isEmpty()) {
            return "No relevant knowledge base documents found.";
        }

        StringBuilder context = new StringBuilder();
        for (int i = 0; i < documents.size(); i++) {
            KnowledgeDocument doc = documents.get(i);
            context.append(String.format("--- Document %d: %s (%s) ---\n",
                    i + 1, doc.getTitle(), doc.getDocType()));
            context.append(doc.getContent());
            context.append("\n\n");
        }
        return context.toString();
    }

    private String buildPrompt(ResolutionRequest request, String context) {
        return String.format("""
                You are an expert incident response analyst. Based on the following context
                from our knowledge base, provide a detailed resolution recommendation.

                CONTEXT FROM KNOWLEDGE BASE:
                %s

                INCIDENT DETAILS:
                - ID: %s
                - Title: %s
                - Description: %s
                - Category: %s
                - Severity: %s

                Please provide your response in the following format:

                ROOT CAUSE:
                [Explain the likely root cause based on the context]

                RESOLUTION STEPS:
                1. [Step 1]
                2. [Step 2]
                3. [Continue as needed]

                PREVENTION TIPS:
                - [Tip 1]
                - [Tip 2]
                - [Continue as needed]

                Be specific and actionable. Reference relevant runbooks or past incidents when applicable.
                """,
                context,
                request.getIncidentId(),
                request.getTitle(),
                request.getDescription(),
                request.getCategory() != null ? request.getCategory() : "Unknown",
                request.getSeverity() != null ? request.getSeverity() : "Unknown");
    }

    private ResolutionResponse parseResponse(String incidentId, String rawResponse,
            List<KnowledgeDocument> relevantDocs) {
        // Parse root cause
        String rootCause = extractSection(rawResponse, "ROOT CAUSE:", "RESOLUTION STEPS:");

        // Parse resolution steps
        List<String> resolutionSteps = extractNumberedList(rawResponse, "RESOLUTION STEPS:", "PREVENTION TIPS:");

        // Parse prevention tips
        List<String> preventionTips = extractBulletList(rawResponse, "PREVENTION TIPS:");

        // Build source document list
        List<ResolutionResponse.SourceDocument> sourceDocs = relevantDocs.stream()
                .map(doc -> ResolutionResponse.SourceDocument.builder()
                        .id(doc.getId())
                        .title(doc.getTitle())
                        .type(doc.getDocType().name())
                        .similarity(0.0) // Would need to compute from DB
                        .build())
                .toList();

        // Calculate confidence based on relevant docs found
        double confidence = relevantDocs.isEmpty() ? 0.3 : Math.min(0.95, 0.5 + (relevantDocs.size() * 0.1));

        return ResolutionResponse.builder()
                .incidentId(incidentId)
                .rootCause(rootCause.trim())
                .resolutionSteps(resolutionSteps)
                .preventionTips(preventionTips)
                .relevantDocuments(sourceDocs)
                .confidence(confidence)
                .rawExplanation(rawResponse)
                .build();
    }

    private String extractSection(String text, String startMarker, String endMarker) {
        int start = text.indexOf(startMarker);
        int end = text.indexOf(endMarker);
        if (start == -1)
            return "Unable to determine root cause.";
        start += startMarker.length();
        if (end == -1)
            end = text.length();
        return text.substring(start, end).trim();
    }

    private List<String> extractNumberedList(String text, String startMarker, String endMarker) {
        String section = extractSection(text, startMarker, endMarker);
        List<String> items = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\d+\\.\\s*(.+?)(?=\\d+\\.|$)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(section);
        while (matcher.find()) {
            items.add(matcher.group(1).trim());
        }
        return items.isEmpty() ? List.of("Follow standard incident response procedures.") : items;
    }

    private List<String> extractBulletList(String text, String startMarker) {
        int start = text.indexOf(startMarker);
        if (start == -1)
            return List.of("Implement monitoring and alerting.");
        String section = text.substring(start + startMarker.length());
        List<String> items = new ArrayList<>();
        Pattern pattern = Pattern.compile("-\\s*(.+?)(?=-|$)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(section);
        while (matcher.find()) {
            items.add(matcher.group(1).trim());
        }
        return items.isEmpty() ? List.of("Implement monitoring and alerting.") : items;
    }
}
