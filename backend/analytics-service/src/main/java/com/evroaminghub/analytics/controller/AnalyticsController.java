package com.evroaminghub.analytics.controller;

import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.evroaminghub.analytics.service.AnalyticsService;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "EV platform analytics, utilization, and revenue dashboards")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/utilization")
    @Operation(summary = "Get station utilization statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'CPO_OPERATOR')")
    public ResponseEntity<Map<String, Object>> getUtilization(
            @RequestParam(required = false) String stationId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "day") String groupBy) {
        return ResponseEntity.ok(analyticsService.getUtilization(stationId, from, to, groupBy));
    }

    @GetMapping("/revenue")
    @Operation(summary = "Get revenue analytics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getRevenue(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "day") String groupBy) {
        return ResponseEntity.ok(analyticsService.getRevenue(from, to, groupBy));
    }

    @GetMapping("/peak-hours")
    @Operation(summary = "Get hourly charging demand heatmap")
    @PreAuthorize("hasAnyRole('ADMIN', 'CPO_OPERATOR')")
    public ResponseEntity<Map<String, Object>> getPeakHours(
            @RequestParam(required = false) String stationId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        return ResponseEntity.ok(analyticsService.getPeakHours(stationId, from, to));
    }

    @GetMapping("/top-stations")
    @Operation(summary = "Get top stations by sessions / revenue / energy")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getTopStations(
            @RequestParam(defaultValue = "revenue") String sortBy,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(analyticsService.getTopStations(sortBy, limit));
    }

    @GetMapping("/demand-forecast")
    @Operation(summary = "Get 7-day demand forecast for a station")
    @PreAuthorize("hasAnyRole('ADMIN', 'CPO_OPERATOR')")
    public ResponseEntity<Map<String, Object>> getDemandForecast(
            @RequestParam(required = false) String stationId) {
        return ResponseEntity.ok(analyticsService.getDemandForecast(stationId));
    }

    @GetMapping("/summary")
    @Operation(summary = "Get platform-wide summary stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getSummary() {
        return ResponseEntity.ok(analyticsService.getPlatformSummary());
    }
}
