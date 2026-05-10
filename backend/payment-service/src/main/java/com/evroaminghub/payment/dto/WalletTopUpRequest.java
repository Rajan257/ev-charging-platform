package com.evroaminghub.payment.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class WalletTopUpRequest {
    private BigDecimal amount;
    private String paymentMethod;
}
