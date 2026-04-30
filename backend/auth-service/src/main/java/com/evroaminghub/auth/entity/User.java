package com.evroaminghub.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true)
    private String phone;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "keycloak_id", unique = true)
    private String keycloakId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.DRIVER;

    @Column(name = "is_active")
    private boolean active = true;

    @Column(name = "email_verified")
    private boolean emailVerified = false;

    @Column(name = "phone_verified")
    private boolean phoneVerified = false;

    @Column(name = "profile_photo")
    private String profilePhoto;

    @Column(name = "preferred_lang", length = 10)
    private String preferredLang = "en";

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<RfidCard> rfidCards = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Vehicle> vehicles = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
