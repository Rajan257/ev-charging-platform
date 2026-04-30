package com.evroaminghub.fraud.engine;

import com.evroaminghub.fraud.entity.FraudAlert;
import com.evroaminghub.fraud.entity.FraudAlert.AlertSeverity;
import com.evroaminghub.fraud.repository.FraudAlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Rule-based fraud detection engine.
 * Each rule is evaluated independently; violations trigger FraudAlert persistence + Kafka event.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FraudDetectionEngine {

    private final FraudAlertRepository alertRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RedisTemplate<String, Object> redisTemplate;

    // ─────────────────────── Rule thresholds ───────────────────────────
    private static final double MAX_ENERGY_KWH             = 150.0;    // max realistic charge
    private static final long   MAX_SESSION_DURATION_HOURS = 6;        // DC fast charge limit
    private static final int    MAX_SESSIONS_PER_HOUR      = 5;        // same token
    private static final double MAX_PAYMENT_REFUND_RATE    = 0.30;     // 30%
    private static final int    MAX_ROAMING_COUNTRIES      = 2;        // geo impossibility

    /**
     * Main entry point — evaluate a completed session event.
     */
    public List<FraudAlert> evaluateSession(Map<String, Object> sessionEvent) {
        List<FraudAlert> alerts = new ArrayList<>();

        String sessionId  = (String) sessionEvent.getOrDefault("sessionId", "unknown");
        String userId     = (String) sessionEvent.getOrDefault("userId",    "unknown");
        String tokenId    = (String) sessionEvent.getOrDefault("tokenId",   "unknown");
        String stationId  = (String) sessionEvent.getOrDefault("stationId", "unknown");

        double energyKwh  = parseDouble(sessionEvent, "energyKwh");
        long   durationMin= parseLong(sessionEvent, "durationMinutes");

        // Rule 1 — Abnormal energy consumption
        if (energyKwh > MAX_ENERGY_KWH) {
            alerts.add(createAlert(sessionId, userId, stationId,
                "ABNORMAL_ENERGY",
                String.format("Session delivered %.1f kWh — exceeds limit of %.0f kWh", energyKwh, MAX_ENERGY_KWH),
                AlertSeverity.HIGH));
        }

        // Rule 2 — Impossible charging duration for DC fast charge
        if (durationMin > MAX_SESSION_DURATION_HOURS * 60) {
            alerts.add(createAlert(sessionId, userId, stationId,
                "IMPOSSIBLE_DURATION",
                String.format("Session lasted %d minutes — exceeds %d hours DC limit", durationMin, MAX_SESSION_DURATION_HOURS),
                AlertSeverity.MEDIUM));
        }

        // Rule 3 — Rapid session rate (same token, high frequency)
        if (tokenId != null && !tokenId.equals("unknown")) {
            String rateKey = "fraud:rate:" + tokenId;
            Long sessionCount = redisTemplate.opsForValue().increment(rateKey);
            if (sessionCount == 1) {
                redisTemplate.expire(rateKey, 1, TimeUnit.HOURS);
            }
            if (sessionCount != null && sessionCount > MAX_SESSIONS_PER_HOUR) {
                alerts.add(createAlert(sessionId, userId, stationId,
                    "RAPID_SESSION_RATE",
                    String.format("Token %s used %d times in 1 hour — max allowed %d", tokenId, sessionCount, MAX_SESSIONS_PER_HOUR),
                    AlertSeverity.HIGH));
            }
        }

        // Rule 4 — Zero-energy session (potential meter tampering)
        if (energyKwh == 0.0 && durationMin > 5) {
            alerts.add(createAlert(sessionId, userId, stationId,
                "ZERO_ENERGY_SESSION",
                String.format("Session ran for %d minutes but delivered 0 kWh", durationMin),
                AlertSeverity.MEDIUM));
        }

        // Persist and broadcast all detected alerts
        alerts.forEach(alert -> {
            alertRepository.save(alert);
            kafkaTemplate.send("fraud.alert.detected", sessionId,
                Map.of("alertId", alert.getId(), "type", alert.getAlertType(),
                       "severity", alert.getSeverity().name(), "sessionId", sessionId,
                       "userId", userId, "description", alert.getDescription()));
            log.warn("FRAUD ALERT [{}] {} — session={}", alert.getSeverity(), alert.getAlertType(), sessionId);
        });

        return alerts;
    }

    /**
     * Evaluate a payment event for anomalies.
     */
    public List<FraudAlert> evaluatePayment(Map<String, Object> paymentEvent) {
        List<FraudAlert> alerts = new ArrayList<>();
        String sessionId = (String) paymentEvent.getOrDefault("sessionId", "unknown");
        String userId    = (String) paymentEvent.getOrDefault("userId",    "unknown");
        double amount    = parseDouble(paymentEvent, "amount");

        // Rule 5 — Abnormally high single payment
        if (amount > 50000) {  // ₹50,000
            alerts.add(createAlert(sessionId, userId, "payment",
                "HIGH_VALUE_PAYMENT",
                String.format("Payment of ₹%.2f is unusually high", amount),
                AlertSeverity.HIGH));
        }

        alerts.forEach(a -> alertRepository.save(a));
        return alerts;
    }

    private FraudAlert createAlert(String sessionId, String userId, String stationId,
                                    String type, String desc, AlertSeverity severity) {
        return FraudAlert.builder()
            .sessionId(sessionId)
            .userId(userId)
            .stationId(stationId)
            .alertType(type)
            .description(desc)
            .severity(severity)
            .status(FraudAlert.AlertStatus.OPEN)
            .detectedAt(Instant.now())
            .build();
    }

    private double parseDouble(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val == null) return 0.0;
        try { return Double.parseDouble(val.toString()); } catch (Exception e) { return 0.0; }
    }

    private long parseLong(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val == null) return 0L;
        try { return Long.parseLong(val.toString()); } catch (Exception e) { return 0L; }
    }
}
