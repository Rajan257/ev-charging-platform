package com.evroaminghub.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "vehicles")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String make;

    @Column(nullable = false)
    private String model;

    private Integer year;

    @Column(name = "registration_number")
    private String registrationNumber;

    @Column(unique = true, length = 17)
    private String vin;

    @Column(name = "contract_id", unique = true)
    private String contractId;   // ISO 15118 EMAID

    @Column(name = "pnc_certificate", columnDefinition = "TEXT")
    private String pncCertificate;

    @Column(name = "is_default")
    private boolean isDefault = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
