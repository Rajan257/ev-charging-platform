package com.evroaminghub.roaming.dto;

import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class OcpiCredentials {
    private String token;
    private String url;
    private String businessDetails;
    private String partyId;
    private String countryCode;
}
