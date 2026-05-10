package com.evroaminghub.payment.dto;

import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ConfirmPaymentRequest {
    private String paymentId;
    private String signature;
}
