package com.evroaminghub.station.controller;

import com.evroaminghub.station.dto.*;
import com.evroaminghub.station.service.StationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/stations")
@RequiredArgsConstructor
@Tag(name = "Charging Stations", description = "Discover and manage EV charging stations")
public class StationController {

    private final StationService stationService;

    @GetMapping
    @Operation(summary = "Search and list charging stations (with optional filters)")
    public ResponseEntity<Page<StationSummaryResponse>> getStations(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String connectorType,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(required = false, defaultValue = "20") Double radiusKm,
            Pageable pageable) {
        return ResponseEntity.ok(stationService.searchStations(city, status, connectorType, lat, lng, radiusKm, pageable));
    }

    @GetMapping("/{stationId}")
    @Operation(summary = "Get full station details including connectors")
    public ResponseEntity<StationDetailResponse> getStation(@PathVariable UUID stationId) {
        return ResponseEntity.ok(stationService.getStationById(stationId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CPO_ADMIN', 'PLATFORM_ADMIN')")
    @Operation(summary = "Register a new charging station (CPO admin)")
    public ResponseEntity<StationDetailResponse> createStation(@Valid @RequestBody CreateStationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(stationService.createStation(request));
    }

    @PutMapping("/{stationId}")
    @PreAuthorize("hasAnyRole('CPO_ADMIN', 'PLATFORM_ADMIN')")
    @Operation(summary = "Update station metadata")
    public ResponseEntity<StationDetailResponse> updateStation(
            @PathVariable UUID stationId,
            @Valid @RequestBody UpdateStationRequest request) {
        return ResponseEntity.ok(stationService.updateStation(stationId, request));
    }

    @GetMapping("/{stationId}/connectors")
    @Operation(summary = "Get all connectors for a station with real-time status")
    public ResponseEntity<List<ConnectorResponse>> getConnectors(@PathVariable UUID stationId) {
        return ResponseEntity.ok(stationService.getConnectors(stationId));
    }

    @GetMapping("/network/{cpoNetworkId}")
    @Operation(summary = "Get all stations for a CPO network")
    public ResponseEntity<List<StationSummaryResponse>> getByNetwork(@PathVariable UUID cpoNetworkId) {
        return ResponseEntity.ok(stationService.getStationsByNetwork(cpoNetworkId));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Operation(summary = "Platform-wide station statistics")
    public ResponseEntity<StationStatsResponse> getStats() {
        return ResponseEntity.ok(stationService.getPlatformStats());
    }
}
