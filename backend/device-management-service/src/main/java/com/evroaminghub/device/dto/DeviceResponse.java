package com.evroaminghub.device.dto;

import lombok.*;
import java.time.Instant;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class DeviceResponse {
    private String id;
    private String chargePointId;
    private String stationId;
    private String status;
    private String firmwareVersion;
    private String manufacturer;
    private String model;
    private Instant lastHeartbeat;
    private Boolean certificateValid;
}

// ---- Nested DTOs in same package ----
