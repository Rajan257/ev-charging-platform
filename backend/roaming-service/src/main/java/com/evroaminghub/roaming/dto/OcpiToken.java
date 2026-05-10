package com.evroaminghub.roaming.dto;

import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class OcpiToken {
    private String uid;
    private String type;
    private String contractId;
    private String visualNumber;
    private String issuer;
    private String groupId;
    private boolean valid;
    private String whitelist;
}
