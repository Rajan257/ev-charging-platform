package com.evroaminghub.session.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "charging_sessions")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ChargingSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "transaction_id", unique = true, nullable = false)
    private String transactionId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "connector_id", nullable = false)
    private UUID connectorId;

    @Column(name = "station_id", nullable = false)
    private UUID stationId;

    @Column(name = "rfid_card_id")
    private UUID rfidCardId;

    @Column(name = "vehicle_id")
    private UUID vehicleId;

    @Column(name = "auth_method")
    private String authMethod;

    @Column(name = "ocpi_token")
    private String ocpiToken;

    @Column(name = "roaming_partner_id")
    private UUID roamingPartnerId;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "stopped_at")
    private Instant stoppedAt;

    @Column(name = "start_meter_wh")
    private Long startMeterWh;

    @Column(name = "stop_meter_wh")
    private Long stopMeterWh;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status;

    @Column(name = "stop_reason")
    private String stopReason;

    @Column(name = "invoice_id")
    private UUID invoiceId;

    @Column(name = "total_amount", precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
