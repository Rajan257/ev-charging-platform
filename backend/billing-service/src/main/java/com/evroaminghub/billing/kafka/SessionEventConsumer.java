package com.evroaminghub.billing.kafka;

import com.evroaminghub.billing.dto.GenerateInvoiceRequest;
import com.evroaminghub.billing.service.BillingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionEventConsumer {

    private final BillingService billingService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "session.ended", groupId = "billing-service")
    public void onSessionEnded(Map<String, Object> payload) {
        try {
            log.info("Received session.ended event: {}", payload);

            String sessionId = (String) payload.get("sessionId");
            String userId = (String) payload.get("userId");
            String startedAt = (String) payload.get("startedAt");
            String stoppedAt = (String) payload.get("stoppedAt");
            long startMeterWh = ((Number) payload.get("startMeterWh")).longValue();
            long stopMeterWh = ((Number) payload.get("stopMeterWh")).longValue();

            double energyKwh = (stopMeterWh - startMeterWh) / 1000.0;

            // Calculate duration in minutes
            java.time.Instant start = java.time.Instant.parse(startedAt);
            java.time.Instant stop = java.time.Instant.parse(stoppedAt);
            long durationMinutes = java.time.Duration.between(start, stop).toMinutes();

            // In production: look up tariff from connector via REST call to station-service
            // For demo: use a default tariff ID from sample data
            UUID defaultTariffId = UUID.fromString("00000000-0001-0001-0001-000000000001");

            GenerateInvoiceRequest request = GenerateInvoiceRequest.builder()
                    .sessionId(UUID.fromString(sessionId))
                    .userId(UUID.fromString(userId))
                    .energyKwh(energyKwh)
                    .durationMinutes(durationMinutes)
                    .sessionStartedAt(start)
                    .sessionStoppedAt(stop)
                    .tariffId(defaultTariffId)
                    .crossState(false)
                    .build();

            billingService.generateInvoice(request);
        } catch (Exception e) {
            log.error("Error processing session.ended event: {}", e.getMessage(), e);
        }
    }
}
