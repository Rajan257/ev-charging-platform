package com.evroaminghub.route.service;

import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class SocEstimationService {

    // Average consumption: 15 kWh per 100 km (typical Indian EV)
    private static final double AVG_KWH_PER_100KM = 15.0;

    public Map<String, Object> estimate(double distanceKm, double currentSocPct,
                                         double vehicleRangeKm, String vehicleModel) {
        // Energy consumed = (distance / 100) * avg consumption
        double energyConsumedKwh = (distanceKm / 100.0) * AVG_KWH_PER_100KM;

        // Battery capacity estimate from range (assuming 15kWh/100km)
        double estimatedCapacityKwh = vehicleRangeKm / 100.0 * AVG_KWH_PER_100KM;

        double socDrop = (energyConsumedKwh / estimatedCapacityKwh) * 100.0;
        double finalSocPct = Math.max(0, currentSocPct - socDrop);
        double remainingRangeKm = vehicleRangeKm * (finalSocPct / 100.0);
        boolean chargingRecommended = finalSocPct < 20.0;

        return Map.of(
            "distanceKm",            distanceKm,
            "initialSocPct",         currentSocPct,
            "estimatedFinalSocPct",  Math.round(finalSocPct * 10.0) / 10.0,
            "socDropPct",            Math.round(socDrop * 10.0) / 10.0,
            "energyConsumedKwh",     Math.round(energyConsumedKwh * 10.0) / 10.0,
            "remainingRangeKm",      Math.round(remainingRangeKm * 10.0) / 10.0,
            "chargingRecommended",   chargingRecommended,
            "vehicleModel",          vehicleModel != null ? vehicleModel : "Generic EV",
            "assumedConsumption",    "15 kWh/100km"
        );
    }
}
