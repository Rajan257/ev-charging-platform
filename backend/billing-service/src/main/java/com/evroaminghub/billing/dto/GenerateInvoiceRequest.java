package com.evroaminghub.billing.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateInvoiceRequest {
    private UUID sessionId;
    private UUID userId;
    private String cpoNetworkId;
    private UUID tariffId;
    private Double energyKwh;
    private Double durationMinutes;
    private Instant sessionStartedAt;
    private Instant sessionStoppedAt;
    private boolean crossState;
}
