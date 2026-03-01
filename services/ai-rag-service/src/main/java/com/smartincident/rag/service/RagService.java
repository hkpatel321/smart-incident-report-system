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
        return String.format(
                """
                        You are an expert incident response analyst. Based on the following context
                        from our knowledge base, provide a detailed resolution recommendation.

                        If the context is not relevant to the incident, use your general knowledge but mention that the context was insufficient.

                        CONTEXT FROM KNOWLEDGE BASE:
                        %s

                        INCIDENT DETAILS:
                        - ID: %s
                        - Title: %s
                        - Description: %s
                        - Category: %s
                        - Severity: %s

                        RESPONSE FORMAT:
                        You must return a strictly valid JSON object. Do not include markdown code blocks (like ```json).
                        The JSON must have the following structure:
                        {
                            "rootCause": "Detailed explanation of the likely root cause...",
                            "resolutionSteps": [
                                "Step 1...",
                                "Step 2..."
                            ],
                            "preventionTips": [
                                "Tip 1...",
                                "Tip 2..."
                            ]
                        }
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

        // Robustly extract JSON from the response.
        // Gemini may wrap it in ```json ... ``` or return it with leading/trailing
        // whitespace/newlines.
        String jsonStr = rawResponse.trim();

        // Try to extract a JSON object using regex (handles any code fence variant)
        Matcher jsonMatcher = Pattern.compile("(?s)\\{.*\\}").matcher(jsonStr);
        if (jsonMatcher.find()) {
            jsonStr = jsonMatcher.group().trim();
        }

        try {
            com.google.gson.Gson gson = new com.google.gson.Gson();
            com.google.gson.JsonObject json = gson.fromJson(jsonStr, com.google.gson.JsonObject.class);

            String rootCause = json.has("rootCause") ? json.get("rootCause").getAsString()
                    : "Unable to determine root cause.";

            List<String> resolutionSteps = new ArrayList<>();
            if (json.has("resolutionSteps")) {
                json.getAsJsonArray("resolutionSteps").forEach(e -> resolutionSteps.add(e.getAsString()));
            }

            List<String> preventionTips = new ArrayList<>();
            if (json.has("preventionTips")) {
                json.getAsJsonArray("preventionTips").forEach(e -> preventionTips.add(e.getAsString()));
            }

            // Build source document list
            List<ResolutionResponse.SourceDocument> sourceDocs = relevantDocs.stream()
                    .map(doc -> ResolutionResponse.SourceDocument.builder()
                            .id(doc.getId())
                            .title(doc.getTitle())
                            .type(doc.getDocType().name())
                            .similarity(0.0)
                            .build())
                    .toList();

            // Calculate confidence
            double confidence = relevantDocs.isEmpty() ? 0.3 : Math.min(0.95, 0.5 + (relevantDocs.size() * 0.1));

            return ResolutionResponse.builder()
                    .incidentId(incidentId)
                    .rootCause(rootCause)
                    .resolutionSteps(resolutionSteps)
                    .preventionTips(preventionTips)
                    .relevantDocuments(sourceDocs)
                    .confidence(confidence)
                    .rawExplanation(rawResponse)
                    .build();

        } catch (Exception e) {
            log.error("Failed to parse AI response as JSON. Fallback to raw text.", e);
            // Fallback: return raw response in root cause and generic steps
            return ResolutionResponse.builder()
                    .incidentId(incidentId)
                    .rootCause("AI Output (Parsing Failed): " + rawResponse)
                    .resolutionSteps(List.of("Review the detailed AI output in the root cause section."))
                    .preventionTips(List.of("See above."))
                    .relevantDocuments(List.of())
                    .confidence(0.5)
                    .rawExplanation(rawResponse)
                    .build();
        }
    }
}
