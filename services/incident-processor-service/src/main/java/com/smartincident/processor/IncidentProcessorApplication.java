package com.smartincident.processor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Incident Processor Service.
 * 
 * This service is responsible for:
 * - Consuming incidents from Kafka
 * - Classifying and processing incidents
 * - Persisting to PostgreSQL
 * - Publishing processed events
 */
@SpringBootApplication
public class IncidentProcessorApplication {

    public static void main(String[] args) {
        SpringApplication.run(IncidentProcessorApplication.class, args);
    }
}
