package com.evroaminghub.device.dto;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class FirmwareUpdateRequest {
    private String firmwareUrl;
    private String targetVersion;
    private String retrieveDate;  // ISO 8601
    private Integer retries;
    private Integer retryInterval;
}
