package com.evroaminghub.auth.dto;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data @Builder
public class UserRegisteredEvent {
    private UUID userId;
    private String email;
    private String fullName;
}
