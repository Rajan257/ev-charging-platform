package com.evroaminghub.smartcharging.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.evroaminghub.smartcharging.service.PowerAllocationService;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/smart-charging")
@RequiredArgsConstructor
@Tag(name = "Smart Charging", description = "Dynamic power allocation and grid load management")
public class SmartChargingController {

    private final PowerAllocationService powerService;

    @GetMapping("/grid/current-load")
    @Operation(summary = "Get current grid load metrics")
    @PreAuthorize("hasAnyRole('ADMIN', 'CPO_OPERATOR')")
    public ResponseEntity<Map<String, Object>> getCurrentGridLoad(
            @RequestParam(required = false) String stationId) {
        return ResponseEntity.ok(powerService.getCurrentGridLoad(stationId));
    }

    @PostMapping("/charging-profiles")
    @Operation(summary = "Create an OCPP smart charging profile")
    @PreAuthorize("hasAnyRole('ADMIN', 'CPO_OPERATOR')")
    public ResponseEntity<Map<String, Object>> createChargingProfile(
            @RequestBody Map<String, Object> profile) {
        return ResponseEntity.ok(powerService.createChargingProfile(profile));
    }

    @GetMapping("/charging-profiles/{stationId}")
    @Operation(summary = "Get active charging profiles for a station")
    @PreAuthorize("hasAnyRole('ADMIN', 'CPO_OPERATOR')")
    public ResponseEntity<Map<String, Object>> getChargingProfiles(
            @PathVariable String stationId) {
        return ResponseEntity.ok(powerService.getChargingProfiles(stationId));
    }

    @PostMapping("/power-limit")
    @Operation(summary = "Set dynamic power limit for a station")
    @PreAuthorize("hasAnyRole('ADMIN', 'CPO_OPERATOR')")
    public ResponseEntity<Map<String, Object>> setPowerLimit(
            @RequestParam String stationId,
            @RequestParam double limitKw,
            @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(powerService.setPowerLimit(stationId, limitKw, reason));
    }

    @PostMapping("/demand-response")
    @Operation(summary = "Trigger demand response event (reduce load)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> triggerDemandResponse(
            @RequestParam double reductionPercent,
            @RequestParam(defaultValue = "15") int durationMinutes) {
        return ResponseEntity.ok(powerService.triggerDemandResponse(reductionPercent, durationMinutes));
    }

    @PostMapping("/schedule")
    @Operation(summary = "Schedule off-peak charging for a session")
    @PreAuthorize("hasAnyRole('ADMIN', 'EV_DRIVER')")
    public ResponseEntity<Map<String, Object>> scheduleCharging(
            @RequestBody Map<String, Object> scheduleRequest) {
        return ResponseEntity.ok(powerService.scheduleCharging(scheduleRequest));
    }
}
