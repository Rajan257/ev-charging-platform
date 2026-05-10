package com.evroaminghub.roaming.dto;

import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class OcpiAuthorizationInfo {
    private String allowed;
    private String token;
    private String location;
}
