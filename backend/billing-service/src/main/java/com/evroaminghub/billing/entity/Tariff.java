package com.evroaminghub.billing.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "tariffs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tariff {
    @Id
    @GeneratedValue
    private UUID id;

    private String name;
    private String networkId;
    private BigDecimal pricePerKwh;
    private BigDecimal pricePerMin;
    private BigDecimal flatFee;
    private BigDecimal minPrice;
    private String currency;
    private boolean active;
}
