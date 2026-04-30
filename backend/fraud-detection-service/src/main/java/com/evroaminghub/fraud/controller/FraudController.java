package com.evroaminghub.fraud.controller;

import com.evroaminghub.fraud.entity.FraudAlert;
import com.evroaminghub.fraud.repository.FraudAlertRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/fraud")
@RequiredArgsConstructor
@Tag(name = "Fraud Detection", description = "Anomaly detection and fraud alert management")
public class FraudController {

    private final FraudAlertRepository alertRepository;

    @GetMapping("/alerts")
    @Operation(summary = "Get all fraud alerts with optional filters")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<FraudAlert>> getAlerts(
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("detectedAt").descending());
        if (severity != null) {
            return ResponseEntity.ok(alertRepository.findBySeverity(
                FraudAlert.AlertSeverity.valueOf(severity), pageable));
        }
        return ResponseEntity.ok(alertRepository.findAll(pageable));
    }

    @GetMapping("/alerts/session/{sessionId}")
    @Operation(summary = "Get fraud alerts for a specific session")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<FraudAlert>> getAlertsForSession(@PathVariable String sessionId) {
        return ResponseEntity.ok(alertRepository.findBySessionId(sessionId));
    }

    @PutMapping("/alerts/{id}/resolve")
    @Operation(summary = "Resolve or dismiss a fraud alert")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FraudAlert> resolveAlert(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        FraudAlert alert = alertRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Alert not found: " + id));
        alert.setStatus(FraudAlert.AlertStatus.valueOf(
            body.getOrDefault("status", "RESOLVED")));
        alert.setResolutionNotes(body.get("notes"));
        alert.setResolvedBy(body.get("resolvedBy"));
        alert.setResolvedAt(java.time.Instant.now());
        return ResponseEntity.ok(alertRepository.save(alert));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get fraud detection statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(Map.of(
            "totalAlerts", alertRepository.count(),
            "openAlerts", alertRepository.countByStatus(FraudAlert.AlertStatus.OPEN),
            "highSeverityOpen", alertRepository.countBySeverityAndStatus(
                FraudAlert.AlertSeverity.HIGH, FraudAlert.AlertStatus.OPEN),
            "criticalOpen", alertRepository.countBySeverityAndStatus(
                FraudAlert.AlertSeverity.CRITICAL, FraudAlert.AlertStatus.OPEN)
        ));
    }
}
