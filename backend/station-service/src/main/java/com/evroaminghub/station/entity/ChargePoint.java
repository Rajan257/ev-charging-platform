package com.evroaminghub.station.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "charge_points")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ChargePoint {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id")
    private ChargingStation station;

    @Column(name = "charge_point_id", unique = true, nullable = false)
    private String chargePointId;

    private String model;
    private String vendor;

    @Column(name = "serial_number")
    private String serialNumber;

    @Column(name = "firmware_version")
    private String firmwareVersion;

    @Column(name = "ocpp_version")
    private String ocppVersion = "2.0.1";

    @Column(name = "connection_status")
    private String connectionStatus = "DISCONNECTED";

    @Column(name = "last_boot_at")
    private Instant lastBootAt;

    @Column(name = "last_heartbeat_at")
    private Instant lastHeartbeatAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
