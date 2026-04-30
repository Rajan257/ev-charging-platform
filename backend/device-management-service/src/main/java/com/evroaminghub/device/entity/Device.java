package com.evroaminghub.device.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "devices")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String chargePointId;        // e.g. "CP-ATHER-BLR-001"

    @Column(nullable = false)
    private String stationId;

    @Enumerated(EnumType.STRING)
    private DeviceStatus status;

    private String firmwareVersion;
    private String manufacturer;
    private String model;
    private String serialNumber;
    private String iccid;               // SIM card
    private String imsi;

    private Instant lastHeartbeat;
    private Instant lastBootNotification;
    private Instant lastHealthCheck;

    private Double latitude;
    private Double longitude;

    @Column(columnDefinition = "TEXT")
    private String certificatePem;       // ISO 15118 cert

    private Boolean certificateValid;
    private Instant certificateExpiry;

    private Instant createdAt;
    private Instant updatedAt;

    @PrePersist
    public void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
        if (status == null) status = DeviceStatus.OFFLINE;
    }

    @PreUpdate
    public void onUpdate() { updatedAt = Instant.now(); }

    public enum DeviceStatus { ONLINE, OFFLINE, FAULTED, BOOTING, UPDATING }
}
