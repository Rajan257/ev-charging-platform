package com.evroaminghub.station.dto;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data @Builder
public class ConnectorResponse {
    private UUID id;
    private String evseId;
    private int connectorNumber;
    private String standard;
    private String powerType;
    private int maxVoltage;
    private int maxAmperage;
    private int maxElectricPower;
    private String status;
    private Instant lastStatusUpdate;
}
