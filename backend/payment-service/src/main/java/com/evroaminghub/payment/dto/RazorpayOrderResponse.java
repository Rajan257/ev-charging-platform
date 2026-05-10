package com.evroaminghub.payment.dto;

import lombok.*;
import java.math.BigDecimal;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class RazorpayOrderResponse {
    private String orderId;
    private BigDecimal amount;
    private String currency;
    private String receipt;
    private String status;
}
