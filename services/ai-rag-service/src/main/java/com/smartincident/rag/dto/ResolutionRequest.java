package com.smartincident.rag.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request for AI-assisted incident resolution.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResolutionRequest {

    @NotBlank(message = "Incident ID is required")
    private String incidentId;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    private String category;
    private String severity;
}
