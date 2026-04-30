package com.evroaminghub.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "rfid_cards")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class RfidCard {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(unique = true, nullable = false)
    private String uid;

    private String label;

    @Column(name = "card_type", length = 20)
    @Enumerated(EnumType.STRING)
    private CardType cardType = CardType.RFID;

    @Column(name = "is_active")
    private boolean active = true;

    @Column(name = "issued_at")
    private Instant issuedAt = Instant.now();

    @Column(name = "expires_at")
    private Instant expiresAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    public enum CardType { RFID, NFC, APP }
}
