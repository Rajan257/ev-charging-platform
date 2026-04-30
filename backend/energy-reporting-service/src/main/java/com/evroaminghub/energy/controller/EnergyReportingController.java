package com.evroaminghub.energy.controller;

import com.evroaminghub.energy.service.EnergyReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Energy Reporting", description = "Energy, carbon, and compliance reports")
public class EnergyReportingController {

    private final EnergyReportService reportService;

    @GetMapping("/energy")
    @Operation(summary = "Get energy delivery report")
    @PreAuthorize("hasAnyRole('ADMIN', 'CPO_OPERATOR')")
    public ResponseEntity<Map<String, Object>> getEnergyReport(
            @RequestParam(required = false) String stationId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        return ResponseEntity.ok(reportService.getEnergyReport(stationId, from, to));
    }

    @GetMapping("/carbon-savings")
    @Operation(summary = "Get carbon emission savings report")
    @PreAuthorize("hasAnyRole('ADMIN', 'CPO_OPERATOR')")
    public ResponseEntity<Map<String, Object>> getCarbonSavings(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        return ResponseEntity.ok(reportService.getCarbonSavingsReport(from, to));
    }

    @GetMapping("/tax-summary")
    @Operation(summary = "Get GST tax summary for a period")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getTaxSummary(
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(reportService.getTaxSummary(year, month));
    }

    @GetMapping("/grid-usage")
    @Operation(summary = "Get grid energy consumption report")
    @PreAuthorize("hasAnyRole('ADMIN', 'CPO_OPERATOR')")
    public ResponseEntity<Map<String, Object>> getGridUsage(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        return ResponseEntity.ok(reportService.getGridUsageReport(from, to));
    }

    @GetMapping("/compliance")
    @Operation(summary = "Get CERC/MNRE compliance report")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getComplianceReport(
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(reportService.getComplianceReport(year, month));
    }
}
