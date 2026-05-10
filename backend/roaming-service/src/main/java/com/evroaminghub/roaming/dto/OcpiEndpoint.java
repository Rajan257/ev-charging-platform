package com.evroaminghub.roaming.dto;

import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class OcpiEndpoint {
    private String identifier;
    private String role;
    private String url;
}
