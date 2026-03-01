package com.smartincident.notification.service;

import com.smartincident.notification.config.KafkaConsumerConfig;
import com.smartincident.notification.entity.NotificationLog;
import com.smartincident.notification.event.IncidentCreatedEvent;
import com.smartincident.notification.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Core notification service.
 *
 * Responsibilities:
 * - Consume incident-created events from Kafka
 * - Send email to the reporter (always)
 * - Send email to the on-call address for HIGH / CRITICAL incidents
 * - Log every send attempt to the database
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationLogRepository logRepository;
    private final JavaMailSender mailSender;

    @Value("${notification.mail.from:noreply@smartincident.local}")
    private String fromAddress;

    @Value("${notification.oncall.email:oncall@smartincident.local}")
    private String onCallEmail;

    @Value("${notification.mail.enabled:true}")
    private boolean mailEnabled;

    private static final List<String> HIGH_SEVERITY_LEVELS = List.of("HIGH", "CRITICAL");

    /**
     * Kafka listener — triggered for every new incident event.
     */
    @KafkaListener(topics = KafkaConsumerConfig.INCIDENT_CREATED_TOPIC, groupId = "${spring.kafka.consumer.group-id}", containerFactory = "kafkaListenerContainerFactory")
    public void onIncidentCreated(IncidentCreatedEvent event) {
        if (event == null || event.getPayload() == null) {
            log.warn("Received null or empty incident event, skipping.");
            return;
        }

        IncidentCreatedEvent.IncidentPayload payload = event.getPayload();
        log.info("Received incident event: id={} title={} severity={}",
                payload.getId(), payload.getTitle(), payload.getSeverity());

        List<NotificationLog> logs = new ArrayList<>();

        // 1. Always notify the reporter
        if (payload.getReporterEmail() != null && !payload.getReporterEmail().isBlank()) {
            NotificationLog reporterLog = sendNotification(
                    payload,
                    payload.getReporterEmail(),
                    "REPORTER",
                    buildReporterSubject(payload),
                    buildReporterBody(payload));
            logs.add(reporterLog);
        } else {
            log.warn("No reporter email for incident {}, skipping reporter notification.", payload.getId());
        }

        // 2. Notify on-call for HIGH / CRITICAL incidents
        String severity = payload.getSeverity() != null ? payload.getSeverity().toUpperCase() : "";
        if (HIGH_SEVERITY_LEVELS.contains(severity)) {
            NotificationLog onCallLog = sendNotification(
                    payload,
                    onCallEmail,
                    "ONCALL",
                    buildOnCallSubject(payload),
                    buildOnCallBody(payload));
            logs.add(onCallLog);
        }

        // Persist all log entries
        logRepository.saveAll(logs);
        log.info("Notification processing complete for incident {}. Sent {} notification(s).",
                payload.getId(), logs.size());
    }

    // ─── Private helpers ────────────────────────────────────────────────────

    private NotificationLog sendNotification(
            IncidentCreatedEvent.IncidentPayload payload,
            String recipient,
            String type,
            String subject,
            String body) {

        NotificationLog.NotificationLogBuilder logBuilder = NotificationLog.builder()
                .incidentId(payload.getId())
                .incidentTitle(payload.getTitle())
                .recipientEmail(recipient)
                .notificationType(type)
                .severity(payload.getSeverity());

        if (!mailEnabled) {
            log.info("[MAIL DISABLED] Would send {} notification to {}: {}", type, recipient, subject);
            return logBuilder.status("SENT").build();
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(recipient);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);

            log.info("Email sent ({}) to {}: {}", type, recipient, subject);
            return logBuilder.status("SENT").build();

        } catch (MailException ex) {
            log.error("Failed to send {} email to {} for incident {}: {}",
                    type, recipient, payload.getId(), ex.getMessage());
            return logBuilder.status("FAILED").errorMessage(ex.getMessage()).build();
        }
    }

    // ─── Email templates ────────────────────────────────────────────────────

    private String buildReporterSubject(IncidentCreatedEvent.IncidentPayload p) {
        return String.format("[Smart Incident] Your incident has been received: %s [%s]",
                p.getTitle(), p.getSeverity());
    }

    private String buildReporterBody(IncidentCreatedEvent.IncidentPayload p) {
        return String.format("""
                Dear Reporter,

                Your incident has been successfully received and is being processed.

                ─────────────────────────────────
                Incident ID   : %s
                Title         : %s
                Description   : %s
                Category      : %s
                Severity      : %s
                Status        : %s
                Reported At   : %s
                ─────────────────────────────────

                Our team will review this incident and take appropriate action.
                You will receive further updates as the situation progresses.

                Regards,
                Smart Incident Response System
                """,
                p.getId(), p.getTitle(), p.getDescription(),
                p.getCategory(), p.getSeverity(), p.getStatus(), p.getCreatedAt());
    }

    private String buildOnCallSubject(IncidentCreatedEvent.IncidentPayload p) {
        return String.format("[ALERT][%s] Incident Requires Immediate Attention: %s",
                p.getSeverity(), p.getTitle());
    }

    private String buildOnCallBody(IncidentCreatedEvent.IncidentPayload p) {
        return String.format("""
                ⚠️  HIGH/CRITICAL INCIDENT ALERT  ⚠️

                An incident with severity [%s] has been reported and requires immediate attention.

                ─────────────────────────────────
                Incident ID   : %s
                Title         : %s
                Description   : %s
                Category      : %s
                Severity      : %s
                Source        : %s
                Reporter      : %s
                Reported At   : %s
                ─────────────────────────────────

                Please review and respond immediately.

                Smart Incident Response System
                """,
                p.getSeverity(),
                p.getId(), p.getTitle(), p.getDescription(),
                p.getCategory(), p.getSeverity(), p.getSource(),
                p.getReporterEmail(), p.getCreatedAt());
    }
}
