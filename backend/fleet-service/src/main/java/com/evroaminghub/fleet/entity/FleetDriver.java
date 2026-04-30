package com.evroaminghub.fleet.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "fleet_drivers")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class FleetDriver {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String fleetId;

    private String userId;           // links to auth-service user
    private String driverName;
    private String email;
    private String phone;
    private String employeeId;
    private String rfidCardId;       // assigned RFID for charging

    @Enumerated(EnumType.STRING)
    private DriverStatus status;

    private Double monthlyLimit;     // optional per-driver spending cap
    private Instant createdAt;

    @PrePersist public void onCreate() { createdAt = Instant.now(); status = DriverStatus.ACTIVE; }

    public enum DriverStatus { ACTIVE, SUSPENDED, REMOVED }
}
