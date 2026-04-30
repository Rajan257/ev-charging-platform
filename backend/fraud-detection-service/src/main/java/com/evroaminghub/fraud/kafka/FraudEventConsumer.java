package com.evroaminghub.fraud.kafka;

import com.evroaminghub.fraud.engine.FraudDetectionEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class FraudEventConsumer {

    private final FraudDetectionEngine engine;

    @KafkaListener(topics = "session.ended", groupId = "fraud-detection-group")
    public void onSessionEnded(Map<String, Object> event) {
        log.debug("Fraud check: session.ended event for session={}", event.get("sessionId"));
        engine.evaluateSession(event);
    }

    @KafkaListener(topics = "payment.completed", groupId = "fraud-detection-group")
    public void onPaymentCompleted(Map<String, Object> event) {
        log.debug("Fraud check: payment.completed event for payment={}", event.get("paymentId"));
        engine.evaluatePayment(event);
    }
}
