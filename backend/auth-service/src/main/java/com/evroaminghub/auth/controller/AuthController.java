package com.evroaminghub.auth.controller;

import com.evroaminghub.auth.dto.*;
import com.evroaminghub.auth.service.AuthService;
import com.evroaminghub.auth.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User registration, login, and token management")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/register")
    @Operation(summary = "Register a new EV driver account")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.registerUser(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Login with email and password")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token using refresh token")
    public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request.getRefreshToken()));
    }

    @PostMapping("/validate")
    @Operation(summary = "Validate a JWT token (internal use by API Gateway)")
    public ResponseEntity<TokenValidationResponse> validate(@RequestBody ValidateTokenRequest request) {
        return ResponseEntity.ok(authService.validateToken(request.getToken()));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout and invalidate tokens")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authHeader) {
        // In production: add token to blocklist / revoke in Keycloak
        return ResponseEntity.ok().build();
    }
}
