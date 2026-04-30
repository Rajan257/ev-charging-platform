package com.evroaminghub.device.dto;

import lombok.*;
import java.time.Instant;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class DiagnosticsResponse {
    private String deviceId;
    private String chargePointId;
    private String manufacturer;
    private String model;
    private String serialNumber;
    private String firmwareVersion;
    private String iccid;
    private String imsi;
    private Instant lastBootNotification;
    private Instant lastHeartbeat;
    private String status;
}
