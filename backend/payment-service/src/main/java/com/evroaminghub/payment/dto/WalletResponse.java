package com.evroaminghub.payment.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class WalletResponse {
    private UUID userId;
    private BigDecimal balance;
    private String currency;
}
