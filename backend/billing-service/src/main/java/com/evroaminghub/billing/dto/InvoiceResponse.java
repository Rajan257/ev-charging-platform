package com.evroaminghub.billing.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceResponse {
    private UUID id;
    private String invoiceNumber;
    private UUID sessionId;
    private UUID userId;
    private Double energyKwh;
    private BigDecimal energyCharge;
    private BigDecimal subtotal;
    private BigDecimal cgstAmount;
    private BigDecimal sgstAmount;
    private BigDecimal igstAmount;
    private BigDecimal totalTax;
    private BigDecimal totalAmount;
    private String currency;
    private String status;
    private Instant billingStart;
    private Instant billingEnd;
    private Instant dueDate;
    private Instant paidAt;
}
