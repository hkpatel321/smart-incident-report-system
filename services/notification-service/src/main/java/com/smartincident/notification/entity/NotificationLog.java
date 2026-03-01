package com.smartincident.notification.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Persists a log entry for every notification attempt.
 */
@Entity
@Table(name = "notification_log", schema = "notification")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "incident_id", nullable = false, length = 100)
    private String incidentId;

    @Column(name = "incident_title", length = 300)
    private String incidentTitle;

    @Column(name = "recipient_email", nullable = false, length = 300)
    private String recipientEmail;

    /**
     * REPORTER — sent to the reporter of the incident.
     * ONCALL — sent to the configured on-call address for HIGH/CRITICAL.
     */
    @Column(name = "notification_type", nullable = false, length = 20)
    private String notificationType;

    @Column(name = "severity", length = 20)
    private String severity;

    /**
     * SENT or FAILED
     */
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at")
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
