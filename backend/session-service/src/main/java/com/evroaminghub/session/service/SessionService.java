package com.evroaminghub.session.service;

import com.evroaminghub.session.dto.*;
import com.evroaminghub.session.entity.ChargingSession;
import com.evroaminghub.session.entity.SessionStatus;
import com.evroaminghub.session.exception.ResourceNotFoundException;
import com.evroaminghub.session.exception.SessionException;
import com.evroaminghub.session.repository.ChargingSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {

    private final ChargingSessionRepository sessionRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public SessionResponse startSession(UUID userId, StartSessionRequest request) {
        // Check if user already has an active session
        sessionRepository.findByUserIdAndStatus(userId, SessionStatus.ACTIVE)
                .ifPresent(s -> {
                    throw new SessionException("User already has an active session: " + s.getId());
                });

        String transactionId = "TX-" + System.currentTimeMillis() + "-" + userId.toString().substring(0, 8);

        ChargingSession session = ChargingSession.builder()
                .transactionId(transactionId)
                .userId(userId)
                .connectorId(request.getConnectorId())
                .stationId(request.getStationId())
                .rfidCardId(request.getRfidCardId())
                .vehicleId(request.getVehicleId())
                .authMethod(request.getAuthMethod() != null ? request.getAuthMethod() : "APP")
                .startMeterWh(0L)
                .status(SessionStatus.ACTIVE)
                .startedAt(Instant.now())
                .build();

        session = sessionRepository.save(session);

        // Publish session started event
        kafkaTemplate.send("session.started", transactionId,
                Map.of("sessionId", session.getId().toString(),
                        "userId", userId.toString(),
                        "connectorId", request.getConnectorId().toString(),
                        "stationId", request.getStationId().toString(),
                        "transactionId", transactionId));

        log.info("Session started: {} for user {} at connector {}", transactionId, userId, request.getConnectorId());
        return toResponse(session);
    }

    @Transactional
    public SessionResponse stopSession(UUID userId, UUID sessionId, StopSessionRequest request) {
        ChargingSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + sessionId));

        if (!session.getUserId().equals(userId) && request == null) {
            throw new SessionException("Not authorized to stop this session");
        }

        if (!SessionStatus.ACTIVE.equals(session.getStatus())) {
            throw new SessionException("Session is not active: " + session.getStatus());
        }

        session.setStatus(SessionStatus.STOPPED_BY_USER);
        session.setStoppedAt(Instant.now());
        session.setStopReason(request != null ? request.getReason() : "USER_REQUEST");

        session = sessionRepository.save(session);

        // Publish session ended event - billing-service will consume this
        kafkaTemplate.send("session.ended", session.getTransactionId(),
                Map.of("sessionId", session.getId().toString(),
                        "userId", userId.toString(),
                        "transactionId", session.getTransactionId(),
                        "startedAt", session.getStartedAt().toString(),
                        "stoppedAt", session.getStoppedAt().toString(),
                        "startMeterWh", session.getStartMeterWh(),
                        "stopMeterWh", session.getStopMeterWh() != null ? session.getStopMeterWh() : 0));

        log.info("Session stopped: {} for user {}", session.getTransactionId(), userId);
        return toResponse(session);
    }

    @Transactional(readOnly = true)
    public SessionResponse getSession(UUID sessionId) {
        return toResponse(sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + sessionId)));
    }

    @Transactional(readOnly = true)
    public SessionResponse getActiveSession(UUID userId) {
        return sessionRepository.findByUserIdAndStatus(userId, SessionStatus.ACTIVE)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("No active session found"));
    }

    @Transactional(readOnly = true)
    public Page<SessionSummaryResponse> getSessionHistory(UUID userId, Pageable pageable) {
        return sessionRepository.findByUserIdOrderByStartedAtDesc(userId, pageable)
                .map(this::toSummary);
    }

    @Transactional(readOnly = true)
    public Page<SessionSummaryResponse> getSessionsByStation(UUID stationId, Pageable pageable) {
        return sessionRepository.findByStationIdOrderByStartedAtDesc(stationId, pageable)
                .map(this::toSummary);
    }

    private SessionResponse toResponse(ChargingSession s) {
        return SessionResponse.builder()
                .id(s.getId())
                .transactionId(s.getTransactionId())
                .userId(s.getUserId())
                .connectorId(s.getConnectorId())
                .stationId(s.getStationId())
                .authMethod(s.getAuthMethod())
                .status(s.getStatus().name())
                .startedAt(s.getStartedAt())
                .stoppedAt(s.getStoppedAt())
                .startMeterWh(s.getStartMeterWh())
                .stopMeterWh(s.getStopMeterWh())
                .energyKwh(s.getStopMeterWh() != null
                        ? (s.getStopMeterWh() - s.getStartMeterWh()) / 1000.0
                        : null)
                .totalAmount(s.getTotalAmount())
                .invoiceId(s.getInvoiceId())
                .build();
    }

    private SessionSummaryResponse toSummary(ChargingSession s) {
        return SessionSummaryResponse.builder()
                .id(s.getId())
                .transactionId(s.getTransactionId())
                .status(s.getStatus().name())
                .startedAt(s.getStartedAt())
                .stoppedAt(s.getStoppedAt())
                .energyKwh(s.getStopMeterWh() != null
                        ? (s.getStopMeterWh() - s.getStartMeterWh()) / 1000.0
                        : null)
                .totalAmount(s.getTotalAmount())
                .stationId(s.getStationId())
                .connectorId(s.getConnectorId())
                .build();
    }
}
