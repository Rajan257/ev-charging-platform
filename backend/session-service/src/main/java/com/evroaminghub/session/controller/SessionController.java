package com.evroaminghub.session.controller;

import com.evroaminghub.session.dto.*;
import com.evroaminghub.session.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Charging Sessions", description = "Start, stop, and monitor EV charging sessions")
public class SessionController {

    private final SessionService sessionService;

    @PostMapping("/start")
    @PreAuthorize("hasAnyRole('DRIVER', 'PLATFORM_ADMIN')")
    @Operation(summary = "Start a charging session")
    public ResponseEntity<SessionResponse> startSession(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody StartSessionRequest request) {
        UUID userId = UUID.fromString(jwt.getClaim("userId").toString());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(sessionService.startSession(userId, request));
    }

    @PostMapping("/{sessionId}/stop")
    @PreAuthorize("hasAnyRole('DRIVER', 'CPO_ADMIN', 'PLATFORM_ADMIN')")
    @Operation(summary = "Stop an active charging session")
    public ResponseEntity<SessionResponse> stopSession(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID sessionId,
            @RequestBody(required = false) StopSessionRequest request) {
        UUID userId = UUID.fromString(jwt.getClaim("userId").toString());
        return ResponseEntity.ok(sessionService.stopSession(userId, sessionId, request));
    }

    @GetMapping("/{sessionId}")
    @Operation(summary = "Get session details")
    public ResponseEntity<SessionResponse> getSession(@PathVariable UUID sessionId) {
        return ResponseEntity.ok(sessionService.getSession(sessionId));
    }

    @GetMapping("/active")
    @PreAuthorize("hasRole('DRIVER')")
    @Operation(summary = "Get currently active session for the logged-in user")
    public ResponseEntity<SessionResponse> getActiveSession(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getClaim("userId").toString());
        return ResponseEntity.ok(sessionService.getActiveSession(userId));
    }

    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('DRIVER', 'PLATFORM_ADMIN')")
    @Operation(summary = "Get session history for the authenticated user")
    public ResponseEntity<Page<SessionSummaryResponse>> getHistory(
            @AuthenticationPrincipal Jwt jwt,
            Pageable pageable) {
        UUID userId = UUID.fromString(jwt.getClaim("userId").toString());
        return ResponseEntity.ok(sessionService.getSessionHistory(userId, pageable));
    }

    @GetMapping("/station/{stationId}")
    @PreAuthorize("hasAnyRole('CPO_ADMIN', 'PLATFORM_ADMIN')")
    @Operation(summary = "Get sessions for a charging station (CPO admin)")
    public ResponseEntity<Page<SessionSummaryResponse>> getStationSessions(
            @PathVariable UUID stationId, Pageable pageable) {
        return ResponseEntity.ok(sessionService.getSessionsByStation(stationId, pageable));
    }
}
