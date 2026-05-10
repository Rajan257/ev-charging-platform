package com.evroaminghub.payment.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class InitiatePaymentResponse {
    private UUID paymentId;
    private String orderId;
    private String razorpayKeyId;
    private BigDecimal amount;
    private String currency;
    private String status;
}
