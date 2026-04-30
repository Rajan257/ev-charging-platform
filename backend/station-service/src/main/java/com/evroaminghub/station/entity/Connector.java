package com.evroaminghub.station.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "connectors")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Connector {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id", nullable = false)
    private ChargingStation station;

    @Column(name = "evse_id", unique = true, nullable = false)
    private String evseId;

    @Column(name = "connector_number")
    private int connectorNumber;

    @Column(nullable = false)
    private String standard;      // CCS2, CHAdeMO, TYPE2, etc.

    @Column(name = "power_type", nullable = false)
    private String powerType;     // DC, AC_3_PHASE, AC_1_PHASE

    @Column(name = "max_voltage")
    private int maxVoltage;

    @Column(name = "max_amperage")
    private int maxAmperage;

    @Column(name = "max_electric_power")
    private int maxElectricPower;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConnectorStatus status = ConnectorStatus.AVAILABLE;

    @Column(name = "tariff_id")
    private UUID tariffId;

    @Column(name = "last_status_update")
    private Instant lastStatusUpdate = Instant.now();

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
