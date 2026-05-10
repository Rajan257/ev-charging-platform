package com.evroaminghub.payment.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PaymentResponse {
    private UUID id;
    private UUID invoiceId;
    private String gateway;
    private String paymentMethod;
    private BigDecimal amount;
    private BigDecimal amountPaid;
    private String currency;
    private String status;
    private Instant initiatedAt;
    private Instant completedAt;
}
