package com.smartincident.ingest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Incident Ingest Service.
 * 
 * This service is responsible for:
 * - Receiving incident reports via REST API
 * - Validating incoming requests
 * - Publishing incidents to Kafka for downstream processing
 */
@SpringBootApplication
public class IncidentIngestApplication {

    public static void main(String[] args) {
        SpringApplication.run(IncidentIngestApplication.class, args);
    }
}
