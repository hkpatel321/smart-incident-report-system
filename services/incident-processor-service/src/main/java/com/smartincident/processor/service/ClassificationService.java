package com.smartincident.processor.service;

import com.smartincident.processor.entity.Category;
import com.smartincident.processor.entity.Severity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Rule-based classification service for incident severity.
 * 
 * Classification rules:
 * 1. Keyword-based escalation (security terms → CRITICAL)
 * 2. Category-based adjustment
 * 3. Metadata-based signals (affected services count)
 */
@Service
@Slf4j
public class ClassificationService {

    // Keywords that indicate critical security issues
    private static final List<String> CRITICAL_KEYWORDS = List.of(
            "breach", "ransomware", "data leak", "unauthorized access",
            "ddos", "attack", "vulnerability", "exploit", "intrusion");

    // Keywords that indicate high severity
    private static final List<String> HIGH_KEYWORDS = List.of(
            "outage", "down", "failure", "crash", "timeout", "unavailable",
            "critical error", "production", "customer impact");

    // Keywords that indicate medium severity
    private static final List<String> MEDIUM_KEYWORDS = List.of(
            "slow", "degraded", "intermittent", "warning", "latency",
            "performance", "delay");

    /**
     * Classify incident severity based on rules.
     *
     * @param title            Incident title
     * @param description      Incident description
     * @param category         Incident category
     * @param originalSeverity Original severity from reporter
     * @param metadata         Additional metadata
     * @return ClassificationResult with severity and confidence
     */
    public ClassificationResult classify(
            String title,
            String description,
            Category category,
            Severity originalSeverity,
            Map<String, Object> metadata) {

        String combinedText = (title + " " + description).toLowerCase();

        Severity classifiedSeverity = originalSeverity;
        double confidence = 0.5;
        String suggestedCategory = null;

        // Rule 1: Check for critical keywords
        if (containsAnyKeyword(combinedText, CRITICAL_KEYWORDS)) {
            classifiedSeverity = Severity.CRITICAL;
            confidence = 0.95;
            suggestedCategory = "SECURITY_INCIDENT";
            log.info("Classified as CRITICAL due to keyword match");
        }
        // Rule 2: Check for high severity keywords
        else if (containsAnyKeyword(combinedText, HIGH_KEYWORDS)) {
            if (originalSeverity.ordinal() < Severity.HIGH.ordinal()) {
                classifiedSeverity = Severity.HIGH;
                confidence = 0.85;
                log.info("Escalated to HIGH due to keyword match");
            }
        }
        // Rule 3: Check for medium keywords
        else if (containsAnyKeyword(combinedText, MEDIUM_KEYWORDS)) {
            if (originalSeverity == Severity.LOW) {
                classifiedSeverity = Severity.MEDIUM;
                confidence = 0.75;
                log.info("Escalated to MEDIUM due to keyword match");
            }
        }

        // Rule 4: Category-based adjustment
        if (category == Category.SECURITY && classifiedSeverity.ordinal() < Severity.HIGH.ordinal()) {
            classifiedSeverity = Severity.HIGH;
            confidence = Math.max(confidence, 0.80);
            log.info("Escalated to HIGH due to SECURITY category");
        }

        // Rule 5: Check metadata for affected services
        if (metadata != null && metadata.containsKey("affectedServices")) {
            Object affected = metadata.get("affectedServices");
            if (affected instanceof List && ((List<?>) affected).size() > 3) {
                if (classifiedSeverity.ordinal() < Severity.HIGH.ordinal()) {
                    classifiedSeverity = Severity.HIGH;
                    confidence = Math.max(confidence, 0.85);
                    log.info("Escalated to HIGH due to multiple affected services");
                }
            }
        }

        // Determine suggested category based on content
        if (suggestedCategory == null) {
            suggestedCategory = suggestCategory(combinedText, category);
        }

        return new ClassificationResult(classifiedSeverity, confidence, suggestedCategory);
    }

    private boolean containsAnyKeyword(String text, List<String> keywords) {
        return keywords.stream().anyMatch(text::contains);
    }

    private String suggestCategory(String text, Category originalCategory) {
        if (text.contains("database") || text.contains("db ") || text.contains("sql")) {
            return "DATABASE_ISSUE";
        }
        if (text.contains("network") || text.contains("connection") || text.contains("dns")) {
            return "NETWORK_ISSUE";
        }
        if (text.contains("memory") || text.contains("cpu") || text.contains("disk")) {
            return "RESOURCE_ISSUE";
        }
        if (text.contains("deploy") || text.contains("release") || text.contains("rollback")) {
            return "DEPLOYMENT_ISSUE";
        }
        return originalCategory.name() + "_GENERAL";
    }

    /**
     * Result of classification containing severity, confidence, and suggested
     * category.
     */
    public record ClassificationResult(
            Severity severity,
            double confidence,
            String suggestedCategory) {
    }
}
