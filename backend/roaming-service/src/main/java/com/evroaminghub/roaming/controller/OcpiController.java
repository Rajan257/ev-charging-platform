package com.evroaminghub.roaming.controller;

import com.evroaminghub.roaming.dto.*;
import com.evroaminghub.roaming.service.OcpiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * OCPI 2.2.1 compliant REST endpoints for EV roaming hub.
 * Each OCPI module (versions, tokens, locations, sessions, CDRs) is exposed here.
 */
@RestController
@RequestMapping("/ocpi")
@RequiredArgsConstructor
@Tag(name = "OCPI Roaming", description = "OCPI 2.2.1 roaming hub endpoints for CPO/MSP integration")
public class OcpiController {

    private final OcpiService ocpiService;

    // --- VERSIONS ---
    @GetMapping("/versions")
    @Operation(summary = "OCPI Versions endpoint")
    public ResponseEntity<OcpiResponse<List<OcpiVersion>>> getVersions() {
        return ResponseEntity.ok(OcpiResponse.success(List.of(
                OcpiVersion.builder().version("2.2.1")
                        .url("http://localhost:8086/ocpi/2.2.1").build()
        )));
    }

    @GetMapping("/2.2.1")
    @Operation(summary = "OCPI module endpoints for version 2.2.1")
    public ResponseEntity<OcpiResponse<List<OcpiEndpoint>>> getEndpoints() {
        return ResponseEntity.ok(OcpiResponse.success(ocpiService.getModuleEndpoints()));
    }

    // --- CREDENTIALS (Token Exchange) ---
    @PostMapping("/2.2.1/credentials")
    @Operation(summary = "Register roaming partner and exchange tokens")
    public ResponseEntity<OcpiResponse<OcpiCredentials>> registerPartner(
            @RequestHeader("Authorization") String token,
            @RequestBody OcpiCredentials credentials) {
        return ResponseEntity.ok(OcpiResponse.success(
                ocpiService.registerPartner(token, credentials)));
    }

    @PutMapping("/2.2.1/credentials")
    @Operation(summary = "Update roaming partner credentials")
    public ResponseEntity<OcpiResponse<OcpiCredentials>> updateCredentials(
            @RequestHeader("Authorization") String token,
            @RequestBody OcpiCredentials credentials) {
        return ResponseEntity.ok(OcpiResponse.success(
                ocpiService.updateCredentials(token, credentials)));
    }

    // --- LOCATIONS (Station Discovery) ---
    @GetMapping("/2.2.1/locations")
    @Operation(summary = "Pull all charging locations (for roaming partners)")
    public ResponseEntity<OcpiResponse<List<OcpiLocation>>> getLocations(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(OcpiResponse.success(
                ocpiService.getLocations(offset, limit)));
    }

    @GetMapping("/2.2.1/locations/{locationId}")
    @Operation(summary = "Get a specific location")
    public ResponseEntity<OcpiResponse<OcpiLocation>> getLocation(@PathVariable String locationId) {
        return ResponseEntity.ok(OcpiResponse.success(
                ocpiService.getLocation(locationId)));
    }

    // --- TOKENS (Token Validation for Roaming) ---
    @PostMapping("/2.2.1/tokens/{uid}/authorize")
    @Operation(summary = "Authorize a roaming token (real-time)")
    public ResponseEntity<OcpiResponse<OcpiAuthorizationInfo>> authorizeToken(
            @PathVariable String uid,
            @RequestParam(defaultValue = "RFID") String tokenType) {
        return ResponseEntity.ok(OcpiResponse.success(
                ocpiService.authorizeToken(uid, tokenType)));
    }

    @GetMapping("/2.2.1/tokens/{uid}")
    @Operation(summary = "Get token info")
    public ResponseEntity<OcpiResponse<OcpiToken>> getToken(
            @PathVariable String uid,
            @RequestParam(defaultValue = "RFID") String type) {
        return ResponseEntity.ok(OcpiResponse.success(
                ocpiService.getToken(uid, type)));
    }

    // --- SESSIONS ---
    @GetMapping("/2.2.1/sessions")
    @Operation(summary = "Get roaming sessions")
    public ResponseEntity<OcpiResponse<List<Map<String, Object>>>> getSessions() {
        return ResponseEntity.ok(OcpiResponse.success(ocpiService.getRoamingSessions()));
    }

    // --- CDRs ---
    @GetMapping("/2.2.1/cdrs")
    @Operation(summary = "Get Charge Detail Records for settlement")
    public ResponseEntity<OcpiResponse<List<Map<String, Object>>>> getCdrs() {
        return ResponseEntity.ok(OcpiResponse.success(ocpiService.getCdrs()));
    }
}
