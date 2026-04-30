package com.evroaminghub.billing.service;

import com.evroaminghub.billing.dto.*;
import com.evroaminghub.billing.entity.*;
import com.evroaminghub.billing.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillingService {

    private final InvoiceRepository invoiceRepository;
    private final TariffRepository tariffRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final BigDecimal GST_RATE = new BigDecimal("18.00");
    private static final String INVOICE_PREFIX = "INV-EV-";

    /**
     * Called when a charging session ends (via Kafka consumer).
     * Generates invoice based on energy consumed and active tariff.
     */
    @Transactional
    public InvoiceResponse generateInvoice(GenerateInvoiceRequest request) {
        Tariff tariff = tariffRepository.findById(request.getTariffId())
                .orElseThrow(() -> new RuntimeException("Tariff not found: " + request.getTariffId()));

        BigDecimal energyKwh = BigDecimal.valueOf(request.getEnergyKwh());
        BigDecimal durationMin = BigDecimal.valueOf(request.getDurationMinutes());

        // Calculate charges
        BigDecimal energyCharge = energyKwh.multiply(tariff.getPricePerKwh())
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal idleCharge = durationMin.multiply(tariff.getPricePerMin())
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal flatFee = tariff.getFlatFee() != null ? tariff.getFlatFee() : BigDecimal.ZERO;

        // Check minimum charge
        BigDecimal subtotal = energyCharge.add(idleCharge).add(flatFee);
        if (tariff.getMinPrice() != null && subtotal.compareTo(tariff.getMinPrice()) < 0) {
            subtotal = tariff.getMinPrice();
        }

        // GST Calculation (IGST for cross-state, CGST+SGST for same state)
        BigDecimal gstAmount = subtotal.multiply(GST_RATE)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal cgst = BigDecimal.ZERO;
        BigDecimal sgst = BigDecimal.ZERO;
        BigDecimal igst = BigDecimal.ZERO;

        if (request.isCrossState()) {
            igst = gstAmount;
        } else {
            cgst = gstAmount.divide(BigDecimal.TWO, 2, RoundingMode.HALF_UP);
            sgst = gstAmount.subtract(cgst);
        }

        BigDecimal totalAmount = subtotal.add(gstAmount).setScale(2, RoundingMode.HALF_UP);

        // Generate invoice number
        String invoiceNumber = INVOICE_PREFIX + System.currentTimeMillis();

        // Build line items
        List<InvoiceLineItem> lineItems = new ArrayList<>();
        lineItems.add(InvoiceLineItem.builder()
                .description("Energy Charge")
                .quantity(request.getEnergyKwh())
                .unit("kWh")
                .unitPrice(tariff.getPricePerKwh())
                .amount(energyCharge)
                .lineType("ENERGY")
                .build());

        if (idleCharge.compareTo(BigDecimal.ZERO) > 0) {
            lineItems.add(InvoiceLineItem.builder()
                    .description("Idle Time Charge")
                    .quantity(request.getDurationMinutes())
                    .unit("min")
                    .unitPrice(tariff.getPricePerMin())
                    .amount(idleCharge)
                    .lineType("IDLE")
                    .build());
        }

        if (flatFee.compareTo(BigDecimal.ZERO) > 0) {
            lineItems.add(InvoiceLineItem.builder()
                    .description("Session Start Fee")
                    .quantity(1.0)
                    .unit("session")
                    .unitPrice(flatFee)
                    .amount(flatFee)
                    .lineType("FLAT_FEE")
                    .build());
        }

        lineItems.add(InvoiceLineItem.builder()
                .description("GST @" + GST_RATE + "%")
                .quantity(1.0)
                .unit("tax")
                .unitPrice(gstAmount)
                .amount(gstAmount)
                .lineType("TAX")
                .build());

        Invoice invoice = Invoice.builder()
                .invoiceNumber(invoiceNumber)
                .sessionId(request.getSessionId())
                .userId(request.getUserId())
                .cpoNetworkId(request.getCpoNetworkId())
                .billingStart(request.getSessionStartedAt())
                .billingEnd(request.getSessionStoppedAt())
                .energyKwh(request.getEnergyKwh())
                .energyCharge(energyCharge)
                .idleCharge(idleCharge)
                .flatCharge(flatFee)
                .subtotal(subtotal)
                .cgstAmount(cgst)
                .sgstAmount(sgst)
                .igstAmount(igst)
                .totalTax(gstAmount)
                .totalAmount(totalAmount)
                .currency("INR")
                .status(InvoiceStatus.PENDING)
                .dueDate(Instant.now().plusSeconds(86400 * 7)) // 7 days
                .lineItems(lineItems)
                .build();

        lineItems.forEach(li -> li.setInvoice(invoice));
        Invoice saved = invoiceRepository.save(invoice);

        log.info("Invoice generated: {} for session {} amount ₹{}",
                invoiceNumber, request.getSessionId(), totalAmount);

        // Notify payment service
        kafkaTemplate.send("invoice.generated", saved.getId().toString(),
                Map.of("invoiceId", saved.getId().toString(),
                        "userId", request.getUserId().toString(),
                        "totalAmount", totalAmount.toString(),
                        "sessionId", request.getSessionId().toString()));

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public InvoiceResponse getInvoice(UUID invoiceId) {
        return toResponse(invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found: " + invoiceId)));
    }

    private InvoiceResponse toResponse(Invoice inv) {
        return InvoiceResponse.builder()
                .id(inv.getId())
                .invoiceNumber(inv.getInvoiceNumber())
                .sessionId(inv.getSessionId())
                .userId(inv.getUserId())
                .energyKwh(inv.getEnergyKwh())
                .energyCharge(inv.getEnergyCharge())
                .subtotal(inv.getSubtotal())
                .cgstAmount(inv.getCgstAmount())
                .sgstAmount(inv.getSgstAmount())
                .igstAmount(inv.getIgstAmount())
                .totalTax(inv.getTotalTax())
                .totalAmount(inv.getTotalAmount())
                .currency(inv.getCurrency())
                .status(inv.getStatus().name())
                .billingStart(inv.getBillingStart())
                .billingEnd(inv.getBillingEnd())
                .dueDate(inv.getDueDate())
                .paidAt(inv.getPaidAt())
                .build();
    }
}
