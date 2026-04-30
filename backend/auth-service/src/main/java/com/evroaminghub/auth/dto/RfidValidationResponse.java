package com.evroaminghub.auth.dto;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data @Builder
public class RfidValidationResponse {
    private boolean valid;
    private UUID userId;
    private UUID cardId;
    private String label;
}
