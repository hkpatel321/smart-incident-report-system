package com.smartincident.rag.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response containing AI-generated resolution recommendation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResolutionResponse {

    private String incidentId;
    private String rootCause;
    private List<String> resolutionSteps;
    private List<String> preventionTips;
    private List<SourceDocument> relevantDocuments;
    private double confidence;
    private String rawExplanation;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SourceDocument {
        private Long id;
        private String title;
        private String type;
        private double similarity;
    }
}
