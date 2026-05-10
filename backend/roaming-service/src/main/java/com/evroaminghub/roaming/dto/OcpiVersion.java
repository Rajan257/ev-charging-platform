package com.evroaminghub.roaming.dto;

import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class OcpiVersion {
    private String version;
    private String url;
}
