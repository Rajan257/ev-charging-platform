package com.evroaminghub.energy.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.Map;

/**
 * Energy reporting service.
 * Carbon offset calculation: 0.82 kg CO2 per kWh (India grid emission factor, CEA 2023).
 * Tax calculations follow India GST framework.
 */
@Service
@Slf4j
public class EnergyReportService {

    // India Grid Emission Factor (CEA 2023): 0.82 kg CO2/kWh
    private static final double INDIA_GRID_EMISSION_FACTOR_KG_PER_KWH = 0.82;

    public Map<String, Object> getEnergyReport(String stationId, String from, String to) {
        // Production: query billing CDRs for the period
        double totalKwh = 12450.5;
        double avgPerSession = 22.5;
        return Map.of(
            "stationId", stationId != null ? stationId : "ALL",
            "period", buildPeriod(from, to),
            "totalEnergyKwh", totalKwh,
            "totalSessions", 554,
            "avgEnergyPerSessionKwh", avgPerSession,
            "peakDemandKw", 150.0,
            "unit", "kWh",
            "generatedAt", Instant.now().toString()
        );
    }

    public Map<String, Object> getCarbonSavingsReport(String from, String to) {
        double totalKwh = 12450.5;
        double carbonSavedKg = totalKwh * INDIA_GRID_EMISSION_FACTOR_KG_PER_KWH;
        double treesEquivalent = carbonSavedKg / 21.77;  // avg CO2 absorbed per tree/year
        double petrolLitresEquivalent = carbonSavedKg / 2.31;  // kg CO2 per litre petrol

        return Map.of(
            "period", buildPeriod(from, to),
            "totalEnergyDeliveredKwh", totalKwh,
            "carbonSavedKg", Math.round(carbonSavedKg * 100.0) / 100.0,
            "carbonSavedTonnes", Math.round(carbonSavedKg / 1000.0 * 100.0) / 100.0,
            "treesEquivalent", Math.round(treesEquivalent),
            "petrolLitresAvoided", Math.round(petrolLitresEquivalent * 10.0) / 10.0,
            "emissionFactorUsed", INDIA_GRID_EMISSION_FACTOR_KG_PER_KWH + " kg CO2/kWh (CEA 2023)",
            "generatedAt", Instant.now().toString()
        );
    }

    public Map<String, Object> getTaxSummary(int year, int month) {
        // Production: aggregate from billing invoice records
        double subtotal = 174307.60;
        double cgst    = subtotal * 0.09;
        double sgst    = subtotal * 0.09;
        double igst    = 0.0;   // inter-state if applicable
        double totalTax = cgst + sgst + igst;

        Map<String, Object> map = new java.util.LinkedHashMap<>();
        map.put("period", String.format("%04d-%02d", year, month));
        map.put("currency", "INR");
        map.put("subtotal", subtotal);
        map.put("cgst9pct", Math.round(cgst * 100.0) / 100.0);
        map.put("sgst9pct", Math.round(sgst * 100.0) / 100.0);
        map.put("igst", igst);
        map.put("totalTaxCollected", Math.round(totalTax * 100.0) / 100.0);
        map.put("grossRevenue", Math.round((subtotal + totalTax) * 100.0) / 100.0);
        map.put("gstFilingRequired", true);
        map.put("gstinForFiling", "29AABCU9603R1ZX");
        map.put("generatedAt", Instant.now().toString());
        return map;
    }

    public Map<String, Object> getGridUsageReport(String from, String to) {
        return Map.of(
            "period", buildPeriod(from, to),
            "totalDrawnKwh", 13200.0,
            "totalDeliveredKwh", 12450.5,
            "chargingEfficiencyPct", 94.3,
            "gridConnectionType", "11kV HT Connection",
            "sanctionedLoadKw", 500,
            "mdKw", 148.5,
            "powerFactor", 0.97,
            "generatedAt", Instant.now().toString()
        );
    }

    public Map<String, Object> getComplianceReport(int year, int month) {
        Map<String, Object> map = new java.util.LinkedHashMap<>();
        map.put("period", String.format("%04d-%02d", year, month));
        map.put("regulatoryBody", "CERC / State DISCOM");
        map.put("totalStationsReporting", 127);
        map.put("totalEnergyKwh", 12450.5);
        map.put("totalSessions", 554);
        map.put("avgAvailabilityPct", 96.8);
        map.put("faultIncidents", 3);
        map.put("resolvedIncidents", 3);
        map.put("openIncidents", 0);
        map.put("complianceStatus", "COMPLIANT");
        map.put("reportingStandard", "MNRE EV Charging Infrastructure Guidelines 2023");
        map.put("generatedAt", Instant.now().toString());
        return map;
    }

    private Map<String, String> buildPeriod(String from, String to) {
        return Map.of(
            "from", from != null ? from : "2026-04-01T00:00:00Z",
            "to",   to   != null ? to   : Instant.now().toString()
        );
    }
}
