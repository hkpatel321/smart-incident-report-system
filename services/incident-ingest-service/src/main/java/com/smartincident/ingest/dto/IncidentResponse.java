package com.smartincident.ingest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Response DTO returned after incident submission.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentResponse {

    private String incidentId;
    private String title;
    private Severity severity;
    private String status;
    private Instant createdAt;
    private String message;
}
