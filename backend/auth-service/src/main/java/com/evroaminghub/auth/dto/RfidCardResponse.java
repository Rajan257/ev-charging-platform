package com.evroaminghub.auth.dto;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data @Builder
public class RfidCardResponse {
    private UUID id;
    private String uid;
    private String label;
    private String cardType;
    private boolean active;
    private Instant issuedAt;
    private Instant expiresAt;
}
