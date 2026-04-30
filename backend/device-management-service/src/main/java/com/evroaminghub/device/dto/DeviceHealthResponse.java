package com.evroaminghub.device.dto;

import lombok.*;
import java.time.Instant;
import java.util.Map;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class DeviceHealthResponse {
    private String deviceId;
    private String chargePointId;
    private String status;
    private boolean isOnline;
    private Instant lastHeartbeat;
    private String firmwareVersion;
    private Boolean certificateValid;
    private Instant certificateExpiry;
    private Map<String, Object> additionalMetrics;
}
