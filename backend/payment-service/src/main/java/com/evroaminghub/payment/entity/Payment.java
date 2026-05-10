package com.evroaminghub.payment.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    @GeneratedValue
    private UUID id;

    private UUID invoiceId;
    private UUID userId;

    @Enumerated(EnumType.STRING)
    private PaymentGateway gateway;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private String gatewayOrderId;
    private String gatewayPaymentId;
    private String gatewaySignature;
    private String upiVpa;
    private BigDecimal amount;
    private BigDecimal amountPaid;
    private String currency;
    private String failureReason;
    private Instant initiatedAt;
    private Instant completedAt;
}
