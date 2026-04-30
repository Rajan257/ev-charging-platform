package com.evroaminghub.fraud.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "fraud_alerts")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class FraudAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String sessionId;

    private String userId;
    private String stationId;
    private String tokenId;

    @Column(nullable = false)
    private String alertType;    // ABNORMAL_ENERGY, IMPOSSIBLE_DURATION, RAPID_SESSION_RATE, etc.

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertSeverity severity;   // LOW, MEDIUM, HIGH, CRITICAL

    @Enumerated(EnumType.STRING)
    private AlertStatus status;       // OPEN, INVESTIGATING, RESOLVED, FALSE_POSITIVE

    private String resolvedBy;
    private String resolutionNotes;
    private Instant resolvedAt;
    private Instant detectedAt;

    public enum AlertSeverity { LOW, MEDIUM, HIGH, CRITICAL }
    public enum AlertStatus   { OPEN, INVESTIGATING, RESOLVED, FALSE_POSITIVE }
}
