package com.evroaminghub.device.controller;

import com.evroaminghub.device.dto.*;
import com.evroaminghub.device.service.DeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/devices")
@RequiredArgsConstructor
@Tag(name = "Device Management", description = "OCPP charger remote control and firmware management")
public class DeviceController {

    private final DeviceService deviceService;

    @GetMapping
    @Operation(summary = "List all registered devices")
    @PreAuthorize("hasAnyRole('ADMIN', 'CPO_OPERATOR')")
    public ResponseEntity<List<DeviceResponse>> getAllDevices(
            @RequestParam(required = false) String stationId,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(deviceService.getDevices(stationId, status));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get device by ID")
    @PreAuthorize("hasAnyRole('ADMIN', 'CPO_OPERATOR')")
    public ResponseEntity<DeviceResponse> getDevice(@PathVariable String id) {
        return ResponseEntity.ok(deviceService.getDevice(id));
    }

    @GetMapping("/{id}/health")
    @Operation(summary = "Get device health metrics")
    @PreAuthorize("hasAnyRole('ADMIN', 'CPO_OPERATOR')")
    public ResponseEntity<DeviceHealthResponse> getDeviceHealth(@PathVariable String id) {
        return ResponseEntity.ok(deviceService.getDeviceHealth(id));
    }

    @PostMapping("/{id}/restart")
    @Operation(summary = "Remote restart charger (OCPP Reset)")
    @PreAuthorize("hasAnyRole('ADMIN', 'CPO_OPERATOR')")
    public ResponseEntity<Map<String, String>> restartDevice(
            @PathVariable String id,
            @RequestParam(defaultValue = "Soft") String resetType) {
        deviceService.restartDevice(id, resetType);
        return ResponseEntity.ok(Map.of(
            "status", "COMMAND_SENT",
            "command", "Reset",
            "resetType", resetType,
            "deviceId", id
        ));
    }

    @PostMapping("/{id}/unlock-connector")
    @Operation(summary = "Unlock a specific connector (OCPP UnlockConnector)")
    @PreAuthorize("hasAnyRole('ADMIN', 'CPO_OPERATOR')")
    public ResponseEntity<Map<String, String>> unlockConnector(
            @PathVariable String id,
            @RequestParam int connectorId) {
        deviceService.unlockConnector(id, connectorId);
        return ResponseEntity.ok(Map.of(
            "status", "COMMAND_SENT",
            "command", "UnlockConnector",
            "connectorId", String.valueOf(connectorId)
        ));
    }

    @PostMapping("/{id}/update-config")
    @Operation(summary = "Change charger configuration (OCPP ChangeConfiguration)")
    @PreAuthorize("hasAnyRole('ADMIN', 'CPO_OPERATOR')")
    public ResponseEntity<Map<String, String>> updateConfig(
            @PathVariable String id,
            @RequestBody ConfigUpdateRequest request) {
        deviceService.updateConfiguration(id, request.getKey(), request.getValue());
        return ResponseEntity.ok(Map.of(
            "status", "COMMAND_SENT",
            "key", request.getKey(),
            "value", request.getValue()
        ));
    }

    @PostMapping("/{id}/firmware-update")
    @Operation(summary = "Trigger firmware update (OCPP UpdateFirmware)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> updateFirmware(
            @PathVariable String id,
            @RequestBody FirmwareUpdateRequest request) {
        deviceService.triggerFirmwareUpdate(id, request);
        return ResponseEntity.ok(Map.of(
            "status", "COMMAND_SENT",
            "firmwareUrl", request.getFirmwareUrl(),
            "targetVersion", request.getTargetVersion()
        ));
    }

    @GetMapping("/{id}/diagnostics")
    @Operation(summary = "Get charger diagnostics (OCPP GetDiagnostics)")
    @PreAuthorize("hasAnyRole('ADMIN', 'CPO_OPERATOR')")
    public ResponseEntity<DiagnosticsResponse> getDiagnostics(@PathVariable String id) {
        return ResponseEntity.ok(deviceService.getDiagnostics(id));
    }

    @PostMapping("/{id}/get-configuration")
    @Operation(summary = "Get charger configuration values")
    @PreAuthorize("hasAnyRole('ADMIN', 'CPO_OPERATOR')")
    public ResponseEntity<Map<String, Object>> getConfiguration(
            @PathVariable String id,
            @RequestParam(required = false) List<String> keys) {
        return ResponseEntity.ok(deviceService.getConfiguration(id, keys));
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new device (called on BootNotification)")
    public ResponseEntity<DeviceResponse> registerDevice(@RequestBody RegisterDeviceRequest request) {
        return ResponseEntity.ok(deviceService.registerDevice(request));
    }
}
