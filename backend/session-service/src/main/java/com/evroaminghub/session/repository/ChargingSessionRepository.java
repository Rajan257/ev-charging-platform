package com.evroaminghub.session.repository;

import com.evroaminghub.session.entity.ChargingSession;
import com.evroaminghub.session.entity.SessionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChargingSessionRepository extends JpaRepository<ChargingSession, UUID> {
    Optional<ChargingSession> findByUserIdAndStatus(UUID userId, SessionStatus status);
    Optional<ChargingSession> findByTransactionId(String transactionId);
    Page<ChargingSession> findByUserIdOrderByStartedAtDesc(UUID userId, Pageable pageable);
    Page<ChargingSession> findByStationIdOrderByStartedAtDesc(UUID stationId, Pageable pageable);
}
