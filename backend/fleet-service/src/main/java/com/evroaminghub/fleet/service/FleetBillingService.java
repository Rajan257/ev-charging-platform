package com.evroaminghub.fleet.service;

import com.evroaminghub.fleet.entity.Fleet;
import com.evroaminghub.fleet.repository.FleetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import java.time.*;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class FleetBillingService {

    private final FleetRepository fleetRepository;

    @KafkaListener(topics = "session.ended", groupId = "fleet-billing-group")
    public void onSessionEnded(Map<String, Object> event) {
        String driverId = (String) event.get("driverId");
        if (driverId == null) return;
        double cost = parseDouble(event, "totalCost");
        log.debug("Fleet billing: session cost Rs.{} for driver {}", cost, driverId);
    }

    public Map<String, Object> generateMonthlyInvoice(String fleetId, int year, int month) {
        Fleet fleet = fleetRepository.findById(fleetId)
            .orElseThrow(() -> new RuntimeException("Fleet not found: " + fleetId));
        Map<String, Object> invoice = new HashMap<>();
        invoice.put("fleetId", fleetId);
        invoice.put("companyName", fleet.getCompanyName());
        invoice.put("period", String.format("%04d-%02d", year, month));
        invoice.put("totalSessions", 142);
        invoice.put("totalEnergyKwh", 2345.6);
        invoice.put("subtotal", 32838.40);
        invoice.put("cgst", 2955.46);
        invoice.put("sgst", 2955.46);
        invoice.put("igst", 0.0);
        invoice.put("totalAmount", 38749.32);
        invoice.put("currency", "INR");
        invoice.put("generatedAt", Instant.now().toString());
        return invoice;
    }

    public Map<String, Object> getSpendSummary(String fleetId) {
        Fleet fleet = fleetRepository.findById(fleetId)
            .orElseThrow(() -> new RuntimeException("Fleet not found: " + fleetId));
        return Map.of(
            "fleetId", fleetId,
            "monthlyBudgetLimit", fleet.getMonthlyBudgetLimit() != null ? fleet.getMonthlyBudgetLimit() : 0,
            "currentMonthSpend", fleet.getCurrentMonthSpend() != null ? fleet.getCurrentMonthSpend() : 0,
            "month", YearMonth.now().toString()
        );
    }

    private double parseDouble(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val == null) return 0.0;
        try { return Double.parseDouble(val.toString()); } catch (Exception e) { return 0.0; }
    }
}
