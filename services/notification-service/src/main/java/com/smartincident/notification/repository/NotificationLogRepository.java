package com.smartincident.notification.repository;

import com.smartincident.notification.entity.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for querying notification log entries.
 */
@Repository
public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {

    List<NotificationLog> findByIncidentIdOrderByCreatedAtDesc(String incidentId);

    List<NotificationLog> findAllByOrderByCreatedAtDesc();
}
