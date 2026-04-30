package com.evroaminghub.smartcharging.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PowerAllocationService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RedisTemplate<String, Object> redisTemplate;

    public Map<String, Object> getCurrentGridLoad(String stationId) {
        // Production: read from grid monitoring system / smart meter API
        double currentLoadKw = 85.0 + new Random().nextDouble() * 40;
        double maxCapacityKw = 200.0;
        double loadPercent   = (currentLoadKw / maxCapacityKw) * 100;
        return Map.of(
            "stationId",         stationId != null ? stationId : "ALL",
            "currentLoadKw",     Math.round(currentLoadKw * 10.0) / 10.0,
            "maxCapacityKw",     maxCapacityKw,
            "loadPercent",       Math.round(loadPercent * 10.0) / 10.0,
            "availableKw",       Math.round((maxCapacityKw - currentLoadKw) * 10.0) / 10.0,
            "gridStatus",        loadPercent < 70 ? "NORMAL" : loadPercent < 90 ? "HIGH" : "CRITICAL",
            "timestamp",         Instant.now().toString()
        );
    }

    public Map<String, Object> createChargingProfile(Map<String, Object> profile) {
        String profileId = UUID.randomUUID().toString();
        profile.put("profileId", profileId);
        profile.put("status", "ACTIVE");
        profile.put("createdAt", Instant.now().toString());

        // Publish to station-service via Kafka to set OCPP SetChargingProfile
        kafkaTemplate.send("smartcharging.profile.set",
            profile.getOrDefault("stationId", "unknown").toString(), profile);

        redisTemplate.opsForValue().set("charging:profile:" + profileId, profile);
        log.info("Charging profile created: {} for station {}", profileId, profile.get("stationId"));
        return profile;
    }

    public Map<String, Object> getChargingProfiles(String stationId) {
        return Map.of(
            "stationId", stationId,
            "profiles", List.of(
                Map.of("profileId","p001","purpose","TxDefaultProfile","chargingRateUnit","kW",
                       "maxChargingRate",50.0,"status","ACTIVE"),
                Map.of("profileId","p002","purpose","ChargePointMaxProfile","chargingRateUnit","kW",
                       "maxChargingRate",150.0,"status","ACTIVE")
            )
        );
    }

    public Map<String, Object> setPowerLimit(String stationId, double limitKw, String reason) {
        Map<String, Object> limitEvent = new HashMap<>();
        limitEvent.put("stationId", stationId);
        limitEvent.put("limitKw", limitKw);
        limitEvent.put("reason", reason != null ? reason : "Manual");
        limitEvent.put("setAt", Instant.now().toString());

        kafkaTemplate.send("smartcharging.power.limit", stationId, limitEvent);
        redisTemplate.opsForValue().set("station:powerlimit:" + stationId, limitKw);

        log.info("Power limit set: {} kW for station {} (reason: {})", limitKw, stationId, reason);
        return Map.of("status", "APPLIED", "stationId", stationId, "limitKw", limitKw);
    }

    public Map<String, Object> triggerDemandResponse(double reductionPercent, int durationMinutes) {
        Map<String, Object> event = Map.of(
            "type",              "DEMAND_RESPONSE",
            "reductionPercent",  reductionPercent,
            "durationMinutes",   durationMinutes,
            "triggeredAt",       Instant.now().toString(),
            "expiresAt",         Instant.now().plusSeconds(durationMinutes * 60L).toString()
        );
        kafkaTemplate.send("smartcharging.demand.response", "ALL", event);
        log.warn("Demand response triggered: {}% reduction for {} minutes", reductionPercent, durationMinutes);
        return Map.of("status", "TRIGGERED", "reductionPercent", reductionPercent,
                      "durationMinutes", durationMinutes, "affectedStations", 127);
    }

    public Map<String, Object> scheduleCharging(Map<String, Object> request) {
        String scheduleId = UUID.randomUUID().toString();
        return Map.of(
            "scheduleId",    scheduleId,
            "status",        "SCHEDULED",
            "preferredStart", request.getOrDefault("preferredStart", "23:00"),
            "targetSocPct",  request.getOrDefault("targetSocPct", 80),
            "estimatedCost", "Rs.85.40 (off-peak rate: Rs.10/kWh)",
            "message",       "Session will start automatically at off-peak hours"
        );
    }
}
