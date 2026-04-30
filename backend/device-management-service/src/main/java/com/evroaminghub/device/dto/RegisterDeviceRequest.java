package com.evroaminghub.device.dto;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class RegisterDeviceRequest {
    private String chargePointId;
    private String stationId;
    private String manufacturer;
    private String model;
    private String serialNumber;
    private String firmwareVersion;
    private String iccid;
    private String imsi;
}
