package com.evroaminghub.auth.service;

import com.evroaminghub.auth.dto.*;
import com.evroaminghub.auth.entity.User;
import com.evroaminghub.auth.exception.AuthenticationException;
import com.evroaminghub.auth.exception.ResourceNotFoundException;
import com.evroaminghub.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthenticationException("Invalid credentials"));

        if (!user.isActive()) {
            throw new AuthenticationException("Account is deactivated");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Failed login attempt for: {}", request.getEmail());
            throw new AuthenticationException("Invalid credentials");
        }

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        log.info("User logged in: {}", user.getEmail());
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(900L)   // 15 minutes in seconds
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .build();
    }

    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtService.validateToken(refreshToken)) {
            throw new AuthenticationException("Invalid or expired refresh token");
        }

        String email = jwtService.extractEmail(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(900L)
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .build();
    }

    public TokenValidationResponse validateToken(String token) {
        boolean valid = jwtService.validateToken(token);
        if (!valid) {
            return TokenValidationResponse.builder().valid(false).build();
        }

        String email = jwtService.extractEmail(token);
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null || !user.isActive()) {
            return TokenValidationResponse.builder().valid(false).build();
        }

        return TokenValidationResponse.builder()
                .valid(true)
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}
