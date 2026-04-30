package com.evroaminghub.simulator.controller;

import com.evroaminghub.simulator.service.SimulatorManagerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/simulator")
@RequiredArgsConstructor
@Tag(name = "OCPP Charger Simulator", description = "Control simulated OCPP chargers for testing")
public class SimulatorController {

    private final SimulatorManagerService manager;

    @PostMapping("/start")
    @Operation(summary = "Start N simulated chargers")
    public ResponseEntity<Map<String, Object>> startSimulation(
            @RequestParam(defaultValue = "5") int count,
            @RequestParam(defaultValue = "ws://localhost:8082/ocpp") String wsUrl) {
        manager.startSimulation(count, wsUrl);
        return ResponseEntity.ok(Map.of(
            "status", "STARTED",
            "chargerCount", count,
            "wsUrl", wsUrl
        ));
    }

    @PostMapping("/stop")
    @Operation(summary = "Stop all simulated chargers")
    public ResponseEntity<Map<String, String>> stopSimulation() {
        manager.stopAll();
        return ResponseEntity.ok(Map.of("status", "STOPPED"));
    }

    @PostMapping("/{chargePointId}/start-transaction")
    @Operation(summary = "Simulate a charging session start")
    public ResponseEntity<Map<String, String>> startTransaction(
            @PathVariable String chargePointId,
            @RequestParam(defaultValue = "SIMULATED-TOKEN-001") String idToken) {
        manager.startTransaction(chargePointId, idToken);
        return ResponseEntity.ok(Map.of("status", "TRANSACTION_STARTED", "idToken", idToken));
    }

    @PostMapping("/{chargePointId}/stop-transaction")
    @Operation(summary = "Simulate a charging session end")
    public ResponseEntity<Map<String, String>> stopTransaction(
            @PathVariable String chargePointId,
            @RequestParam(defaultValue = "Local") String reason) {
        manager.stopTransaction(chargePointId, reason);
        return ResponseEntity.ok(Map.of("status", "TRANSACTION_STOPPED"));
    }

    @PostMapping("/{chargePointId}/fault")
    @Operation(summary = "Simulate a charger fault")
    public ResponseEntity<Map<String, String>> simulateFault(
            @PathVariable String chargePointId,
            @RequestParam(defaultValue = "ConnectorFault") String faultCode) {
        manager.simulateFault(chargePointId, faultCode);
        return ResponseEntity.ok(Map.of("status", "FAULT_SIMULATED", "faultCode", faultCode));
    }

    @GetMapping("/status")
    @Operation(summary = "Get status of all simulated chargers")
    public ResponseEntity<Map<String, Object>> getStatus() {
        return ResponseEntity.ok(manager.getStatus());
    }
}
