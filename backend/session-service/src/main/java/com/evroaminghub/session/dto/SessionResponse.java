package com.evroaminghub.session.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data @Builder
public class SessionResponse {
    private UUID id;
    private String transactionId;
    private UUID userId;
    private UUID connectorId;
    private UUID stationId;
    private String authMethod;
    private String status;
    private Instant startedAt;
    private Instant stoppedAt;
    private Long startMeterWh;
    private Long stopMeterWh;
    private Double energyKwh;
    private BigDecimal totalAmount;
    private UUID invoiceId;
}
