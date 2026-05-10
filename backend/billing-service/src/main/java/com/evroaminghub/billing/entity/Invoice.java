package com.evroaminghub.billing.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "invoices")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Invoice {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(unique = true)
    private String invoiceNumber;

    private UUID sessionId;
    private UUID userId;
    private String cpoNetworkId;
    private Instant billingStart;
    private Instant billingEnd;
    private Double energyKwh;
    private BigDecimal energyCharge;
    private BigDecimal idleCharge;
    private BigDecimal flatCharge;
    private BigDecimal subtotal;
    private BigDecimal cgstAmount;
    private BigDecimal sgstAmount;
    private BigDecimal igstAmount;
    private BigDecimal totalTax;
    private BigDecimal totalAmount;
    private String currency;

    @Enumerated(EnumType.STRING)
    private InvoiceStatus status;

    private Instant dueDate;
    private Instant paidAt;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InvoiceLineItem> lineItems;
}
