package com.evroaminghub.station.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "cpo_networks")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class CpoNetwork {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false, length = 20)
    private String code;

    private String website;
    private String supportEmail;
    private String supportPhone;
    private String partyId;
    private String countryCode = "IN";
    private boolean active = true;
    private String logoUrl;

    @OneToMany(mappedBy = "cpoNetwork", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ChargingStation> stations = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
