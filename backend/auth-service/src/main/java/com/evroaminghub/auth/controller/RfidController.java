package com.evroaminghub.auth.controller;

import com.evroaminghub.auth.dto.*;
import com.evroaminghub.auth.service.RfidService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/rfid")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "RFID Cards", description = "Manage RFID/NFC cards for EV charging authentication")
public class RfidController {

    private final RfidService rfidService;

    @GetMapping
    @PreAuthorize("hasAnyRole('DRIVER', 'PLATFORM_ADMIN')")
    @Operation(summary = "List all RFID cards for the authenticated user")
    public ResponseEntity<List<RfidCardResponse>> getMyCards(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getClaim("userId"));
        return ResponseEntity.ok(rfidService.getCardsByUser(userId));
    }

    @PostMapping
    @PreAuthorize("hasRole('DRIVER')")
    @Operation(summary = "Register a new RFID card")
    public ResponseEntity<RfidCardResponse> registerCard(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody RegisterRfidRequest request) {
        UUID userId = UUID.fromString(jwt.getClaim("userId"));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(rfidService.registerCard(userId, request));
    }

    @DeleteMapping("/{cardId}")
    @PreAuthorize("hasAnyRole('DRIVER', 'PLATFORM_ADMIN')")
    @Operation(summary = "Deactivate an RFID card")
    public ResponseEntity<Void> deactivateCard(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID cardId) {
        UUID userId = UUID.fromString(jwt.getClaim("userId"));
        rfidService.deactivateCard(userId, cardId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/validate")
    @Operation(summary = "Validate an RFID card UID (used by OCPP service)")
    public ResponseEntity<RfidValidationResponse> validateCard(@RequestBody ValidateRfidRequest request) {
        return ResponseEntity.ok(rfidService.validateCard(request.getUid()));
    }
}
