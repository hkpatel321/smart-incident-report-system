package com.smartincident.processor.controller;

import com.smartincident.processor.dto.IncidentDetailDto;
import com.smartincident.processor.dto.IncidentSummaryDto;
import com.smartincident.processor.dto.PagedResponse;
import com.smartincident.processor.entity.Incident;
import com.smartincident.processor.entity.Severity;
import com.smartincident.processor.entity.Status;
import com.smartincident.processor.repository.IncidentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for dashboard incident views.
 * Provides paginated, filterable incident data.
 */
@RestController
@RequestMapping("/api/dashboard/incidents")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

        private final IncidentRepository incidentRepository;

        private static final int DEFAULT_PAGE_SIZE = 10;
        private static final int MAX_PAGE_SIZE = 100;

        /**
         * Get paginated list of incidents with optional filters.
         *
         * @param page     Page number (0-based)
         * @param size     Page size (default 10, max 100)
         * @param severity Filter by severity (optional)
         * @param status   Filter by status (optional)
         * @param search   Search term for title (optional)
         * @param sortBy   Sort field (default: createdAt)
         * @param sortDir  Sort direction: asc/desc (default: desc)
         */
        @GetMapping
        public ResponseEntity<PagedResponse<IncidentSummaryDto>> getIncidents(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(required = false) Severity severity,
                        @RequestParam(required = false) Status status,
                        @RequestParam(required = false) String search,
                        @RequestParam(defaultValue = "createdAt") String sortBy,
                        @RequestParam(defaultValue = "desc") String sortDir) {

                // Validate page size
                size = Math.min(size, MAX_PAGE_SIZE);
                if (size < 1)
                        size = DEFAULT_PAGE_SIZE;

                // Build sort
                Sort sort = sortDir.equalsIgnoreCase("asc")
                                ? Sort.by(sortBy).ascending()
                                : Sort.by(sortBy).descending();
                Pageable pageable = PageRequest.of(page, size, sort);

                // Execute query
                Page<Incident> incidentPage;
                if (search != null && !search.isBlank()) {
                        incidentPage = incidentRepository.searchIncidents(status, severity, search.trim(), pageable);
                } else {
                        incidentPage = incidentRepository.findWithFilters(status, severity, pageable);
                }

                // Convert to DTOs
                List<IncidentSummaryDto> dtos = incidentPage.getContent().stream()
                                .map(IncidentSummaryDto::fromEntity)
                                .toList();

                log.debug("Fetched {} incidents (page {}/{})",
                                dtos.size(), page + 1, incidentPage.getTotalPages());

                return ResponseEntity.ok(PagedResponse.of(
                                dtos,
                                page,
                                size,
                                incidentPage.getTotalElements()));
        }

        /**
         * Get single incident by ID.
         */
        @GetMapping("/{id}")
        public ResponseEntity<IncidentDetailDto> getIncidentById(@PathVariable String id) {
                return incidentRepository.findById(id)
                                .map(IncidentDetailDto::fromEntity)
                                .map(ResponseEntity::ok)
                                .orElse(ResponseEntity.notFound().build());
        }

        /**
         * Get critical incidents (high priority view).
         */
        @GetMapping("/critical")
        public ResponseEntity<PagedResponse<IncidentSummaryDto>> getCriticalIncidents(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size) {

                size = Math.min(size, MAX_PAGE_SIZE);
                Pageable pageable = PageRequest.of(page, size);

                Page<Incident> incidentPage = incidentRepository.findCriticalIncidents(pageable);

                List<IncidentSummaryDto> dtos = incidentPage.getContent().stream()
                                .map(IncidentSummaryDto::fromEntity)
                                .toList();

                return ResponseEntity.ok(PagedResponse.of(
                                dtos,
                                page,
                                size,
                                incidentPage.getTotalElements()));
        }

        /**
         * Get incidents by status (quick filter).
         */
        @GetMapping("/status/{status}")
        public ResponseEntity<PagedResponse<IncidentSummaryDto>> getByStatus(
                        @PathVariable Status status,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {

                size = Math.min(size, MAX_PAGE_SIZE);
                Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

                Page<Incident> incidentPage = incidentRepository.findByStatus(status, pageable);

                List<IncidentSummaryDto> dtos = incidentPage.getContent().stream()
                                .map(IncidentSummaryDto::fromEntity)
                                .toList();

                return ResponseEntity.ok(PagedResponse.of(
                                dtos,
                                page,
                                size,
                                incidentPage.getTotalElements()));
        }

        /**
         * Get incidents by severity (quick filter).
         */
        @GetMapping("/severity/{severity}")
        public ResponseEntity<PagedResponse<IncidentSummaryDto>> getBySeverity(
                        @PathVariable Severity severity,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {

                size = Math.min(size, MAX_PAGE_SIZE);
                Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

                Page<Incident> incidentPage = incidentRepository.findByClassifiedSeverity(severity, pageable);

                List<IncidentSummaryDto> dtos = incidentPage.getContent().stream()
                                .map(IncidentSummaryDto::fromEntity)
                                .toList();

                return ResponseEntity.ok(PagedResponse.of(
                                dtos,
                                page,
                                size,
                                incidentPage.getTotalElements()));
        }

        /**
         * Resolve an incident — ADMIN only.
         */
        @PatchMapping("/{id}/resolve")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<IncidentDetailDto> resolveIncident(
                        @PathVariable String id,
                        @RequestBody(required = false) Map<String, String> body) {

                return incidentRepository.findById(id)
                                .map(incident -> {
                                        incident.setStatus(Status.RESOLVED);
                                        incident.setResolvedAt(Instant.now());
                                        if (body != null && body.containsKey("aiRecommendation")) {
                                                incident.setAiRecommendation(body.get("aiRecommendation"));
                                        }
                                        String existingLogs = incident.getLogs() != null ? incident.getLogs() : "";
                                        incident.setLogs(existingLogs +
                                                        String.format("[%s] Incident resolved%n", Instant.now()));
                                        incidentRepository.save(incident);
                                        log.info("Incident {} resolved", id);
                                        return ResponseEntity.ok(IncidentDetailDto.fromEntity(incident));
                                })
                                .orElse(ResponseEntity.notFound().build());
        }

        /**
         * Assign incident to a user — ADMIN only.
         */
        @PatchMapping("/{id}/assign")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<IncidentDetailDto> assignIncident(
                        @PathVariable String id,
                        @RequestBody Map<String, String> body) {

                String assignee = body.get("assignedTo");
                if (assignee == null || assignee.isBlank()) {
                        return ResponseEntity.badRequest().build();
                }

                return incidentRepository.findById(id)
                                .map(incident -> {
                                        incident.setAssignedTo(assignee);
                                        String existingLogs = incident.getLogs() != null ? incident.getLogs() : "";
                                        incident.setLogs(existingLogs +
                                                        String.format("[%s] Assigned to %s%n", Instant.now(),
                                                                        assignee));
                                        incidentRepository.save(incident);
                                        log.info("Incident {} assigned to {}", id, assignee);
                                        return ResponseEntity.ok(IncidentDetailDto.fromEntity(incident));
                                })
                                .orElse(ResponseEntity.notFound().build());
        }

        /**
         * Update incident status.
         */
        @PatchMapping("/{id}/status")
        public ResponseEntity<IncidentDetailDto> updateStatus(
                        @PathVariable String id,
                        @RequestBody Map<String, String> body) {

                String statusStr = body.get("status");
                if (statusStr == null) {
                        return ResponseEntity.badRequest().build();
                }

                Status newStatus;
                try {
                        newStatus = Status.valueOf(statusStr.toUpperCase());
                } catch (IllegalArgumentException e) {
                        return ResponseEntity.badRequest().build();
                }

                return incidentRepository.findById(id)
                                .map(incident -> {
                                        incident.setStatus(newStatus);
                                        if (newStatus == Status.RESOLVED) {
                                                incident.setResolvedAt(Instant.now());
                                        }
                                        incidentRepository.save(incident);
                                        log.info("Incident {} status updated to {}", id, newStatus);
                                        return ResponseEntity.ok(IncidentDetailDto.fromEntity(incident));
                                })
                                .orElse(ResponseEntity.notFound().build());
        }

        /**
         * Update incident — ADMIN can update any; DEVELOPER only their own.
         */
        @PutMapping("/{id}")
        public ResponseEntity<IncidentDetailDto> updateIncident(
                        @PathVariable String id,
                        @RequestBody com.smartincident.processor.dto.IncidentUpdateRequest request,
                        Authentication auth) {

                return incidentRepository.findById(id)
                                .map(incident -> {
                                        // Ownership check for DEVELOPER
                                        if (!isAdminOrOwner(auth, incident)) {
                                                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                                                .<IncidentDetailDto>build();
                                        }
                                        if (request.getTitle() != null)
                                                incident.setTitle(request.getTitle());
                                        if (request.getDescription() != null)
                                                incident.setDescription(request.getDescription());
                                        if (request.getCategory() != null)
                                                incident.setCategory(request.getCategory());
                                        if (request.getSeverity() != null)
                                                incident.setClassifiedSeverity(request.getSeverity());
                                        if (request.getStatus() != null) {
                                                incident.setStatus(request.getStatus());
                                                if (request.getStatus() == Status.RESOLVED
                                                                && incident.getResolvedAt() == null) {
                                                        incident.setResolvedAt(Instant.now());
                                                }
                                        }
                                        if (request.getAssignedTo() != null)
                                                incident.setAssignedTo(request.getAssignedTo());

                                        incidentRepository.save(incident);
                                        log.info("Incident {} updated by {}", id, auth.getPrincipal());
                                        return ResponseEntity.ok(IncidentDetailDto.fromEntity(incident));
                                })
                                .orElse(ResponseEntity.notFound().build());
        }

        /**
         * Delete incident — ADMIN can delete any; DEVELOPER only their own.
         */
        @DeleteMapping("/{id}")
        public ResponseEntity<Void> deleteIncident(@PathVariable String id, Authentication auth) {
                var incidentOpt = incidentRepository.findById(id);
                if (incidentOpt.isEmpty()) {
                        return ResponseEntity.notFound().build();
                }
                Incident incident = incidentOpt.get();
                if (!isAdminOrOwner(auth, incident)) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
                incidentRepository.deleteById(id);
                log.info("Incident {} deleted by {}", id, auth.getPrincipal());
                return ResponseEntity.noContent().build();
        }

        // ─── Helpers ────────────────────────────────────────────────────────────

        /**
         * Returns true if the caller is an ADMIN, or is the reporter of the incident.
         */
        private boolean isAdminOrOwner(Authentication auth, Incident incident) {
                if (auth == null)
                        return false;
                boolean isAdmin = auth.getAuthorities().stream()
                                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                if (isAdmin)
                        return true;
                String callerEmail = (String) auth.getPrincipal();
                return callerEmail != null && callerEmail.equalsIgnoreCase(incident.getReporterEmail());
        }
}
