package com.evroaminghub.fleet.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "fleets")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Fleet {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String companyName;

    @Column(nullable = false, unique = true)
    private String gstNumber;

    private String contactEmail;
    private String contactPhone;
    private String address;

    @Enumerated(EnumType.STRING)
    private FleetStatus status;

    private Double monthlyBudgetLimit;   // ₹ per month
    private Double currentMonthSpend;

    private Instant createdAt;
    private Instant updatedAt;

    @PrePersist  public void onCreate() { createdAt = Instant.now(); updatedAt = Instant.now(); status = FleetStatus.ACTIVE; }
    @PreUpdate   public void onUpdate() { updatedAt = Instant.now(); }

    public enum FleetStatus { ACTIVE, SUSPENDED, TERMINATED }
}
