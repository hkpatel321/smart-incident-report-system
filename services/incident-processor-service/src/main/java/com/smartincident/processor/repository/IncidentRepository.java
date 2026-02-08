package com.smartincident.processor.repository;

import com.smartincident.processor.entity.Incident;
import com.smartincident.processor.entity.Severity;
import com.smartincident.processor.entity.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Repository for incident persistence operations.
 */
@Repository
public interface IncidentRepository extends JpaRepository<Incident, String> {

    // ========== Basic Queries ==========

    List<Incident> findByStatus(Status status);

    List<Incident> findByClassifiedSeverity(Severity severity);

    List<Incident> findByCreatedAtAfter(Instant since);

    // ========== Paginated Queries ==========

    Page<Incident> findAll(Pageable pageable);

    Page<Incident> findByStatus(Status status, Pageable pageable);

    Page<Incident> findByClassifiedSeverity(Severity severity, Pageable pageable);

    Page<Incident> findByStatusAndClassifiedSeverity(Status status, Severity severity, Pageable pageable);

    // ========== Filtered Queries ==========

    @Query("SELECT i FROM Incident i WHERE " +
            "(:status IS NULL OR i.status = :status) AND " +
            "(:severity IS NULL OR i.classifiedSeverity = :severity) " +
            "ORDER BY i.createdAt DESC")
    Page<Incident> findWithFilters(
            @Param("status") Status status,
            @Param("severity") Severity severity,
            Pageable pageable);

    @Query("SELECT i FROM Incident i WHERE " +
            "(:status IS NULL OR i.status = :status) AND " +
            "(:severity IS NULL OR i.classifiedSeverity = :severity) AND " +
            "(:searchTerm IS NULL OR LOWER(i.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Incident> searchIncidents(
            @Param("status") Status status,
            @Param("severity") Severity severity,
            @Param("searchTerm") String searchTerm,
            Pageable pageable);

    // ========== Count Queries ==========

    @Query("SELECT COUNT(i) FROM Incident i WHERE i.status = :status")
    long countByStatus(@Param("status") Status status);

    @Query("SELECT COUNT(i) FROM Incident i WHERE i.classifiedSeverity = :severity")
    long countBySeverity(@Param("severity") Severity severity);

    // ========== Urgent Queries ==========

    @Query("SELECT i FROM Incident i WHERE i.classifiedSeverity IN ('HIGH', 'CRITICAL') AND i.status = 'NEW'")
    List<Incident> findUrgentUnprocessedIncidents();

    @Query("SELECT i FROM Incident i WHERE i.classifiedSeverity = 'CRITICAL' ORDER BY i.createdAt DESC")
    Page<Incident> findCriticalIncidents(Pageable pageable);
}
