package com.evroaminghub.session.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data @Builder
public class SessionSummaryResponse {
    private UUID id;
    private String transactionId;
    private String status;
    private Instant startedAt;
    private Instant stoppedAt;
    private Double energyKwh;
    private BigDecimal totalAmount;
    private UUID stationId;
    private UUID connectorId;
}
