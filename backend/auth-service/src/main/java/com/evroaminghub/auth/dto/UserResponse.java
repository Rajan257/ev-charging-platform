package com.evroaminghub.auth.dto;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data @Builder
public class UserResponse {
    private UUID id;
    private String email;
    private String phone;
    private String fullName;
    private String role;
    private boolean active;
    private boolean emailVerified;
    private boolean phoneVerified;
    private String profilePhoto;
    private String preferredLang;
    private Instant createdAt;
}
