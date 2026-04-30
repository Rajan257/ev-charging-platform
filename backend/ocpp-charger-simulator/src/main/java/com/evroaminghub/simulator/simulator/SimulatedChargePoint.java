package com.evroaminghub.simulator.simulator;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simulates an OCPP 2.0.1 compliant charger via WebSocket.
 * Sends: BootNotification, Heartbeat, StatusNotification, TransactionEvent, MeterValues.
 */
@Slf4j
public class SimulatedChargePoint extends TextWebSocketHandler {

    private final String chargePointId;
    private final String wsUrl;
    private final ObjectMapper mapper = new ObjectMapper();
    private WebSocketSession session;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final AtomicInteger messageId = new AtomicInteger(1);
    private boolean activeTransaction = false;
    private String currentTransactionId;

    public SimulatedChargePoint(String chargePointId, String wsUrl) {
        this.chargePointId = chargePointId;
        this.wsUrl = wsUrl;
    }

    public void connect() throws Exception {
        StandardWebSocketClient client = new StandardWebSocketClient();
        client.execute(this, wsUrl + "/" + chargePointId).get(10, TimeUnit.SECONDS);
        log.info("[{}] Connected to OCPP server", chargePointId);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        this.session = session;
        sendBootNotification();
        // Schedule heartbeat every 30 seconds
        scheduler.scheduleAtFixedRate(this::sendHeartbeat, 30, 30, TimeUnit.SECONDS);
    }

    private void sendBootNotification() {
        Map<String, Object> payload = Map.of(
            "reason", "PowerUp",
            "chargingStation", Map.of(
                "model", "SimCharger-3000",
                "vendorName", "EVSimCorp",
                "firmwareVersion", "1.0.0-sim",
                "serialNumber", chargePointId
            )
        );
        send("BootNotification", payload);
        sendStatusNotification(1, "Available");
    }

    private void sendHeartbeat() {
        send("Heartbeat", Map.of());
    }

    public void sendStatusNotification(int connectorId, String status) {
        Map<String, Object> payload = Map.of(
            "timestamp", Instant.now().toString(),
            "connectorStatus", status,
            "evseId", connectorId
        );
        send("StatusNotification", payload);
    }

    public void startTransaction(String idToken) {
        if (activeTransaction) return;
        currentTransactionId = UUID.randomUUID().toString();
        activeTransaction = true;
        sendStatusNotification(1, "Occupied");

        Map<String, Object> payload = Map.of(
            "eventType", "Started",
            "timestamp", Instant.now().toString(),
            "triggerReason", "Authorized",
            "seqNo", 1,
            "transactionInfo", Map.of(
                "transactionId", currentTransactionId,
                "chargingState", "Charging"
            ),
            "idToken", Map.of("idToken", idToken, "type", "Central"),
            "evse", Map.of("id", 1, "connectorId", 1)
        );
        send("TransactionEvent", payload);

        // Simulate meter values every minute
        scheduler.scheduleAtFixedRate(() -> sendMeterValues(
            ThreadLocalRandom.current().nextDouble(5, 15)),
            1, 1, TimeUnit.MINUTES);
        log.info("[{}] Transaction started: {}", chargePointId, currentTransactionId);
    }

    public void stopTransaction(String reason) {
        if (!activeTransaction) return;
        activeTransaction = false;
        sendStatusNotification(1, "Available");

        Map<String, Object> payload = Map.of(
            "eventType", "Ended",
            "timestamp", Instant.now().toString(),
            "triggerReason", reason != null ? reason : "Local",
            "seqNo", 99,
            "transactionInfo", Map.of(
                "transactionId", currentTransactionId,
                "stoppedReason", reason != null ? reason : "Local",
                "chargingState", "SuspendedEVSE"
            )
        );
        send("TransactionEvent", payload);
        log.info("[{}] Transaction ended: {}", chargePointId, currentTransactionId);
    }

    private void sendMeterValues(double powerKw) {
        Map<String, Object> payload = Map.of(
            "evseId", 1,
            "transactionId", currentTransactionId != null ? currentTransactionId : "",
            "timestamp", Instant.now().toString(),
            "sampledValue", List.of(
                Map.of("value", powerKw, "measurand", "Power.Active.Import", "unit", "kW"),
                Map.of("value", ThreadLocalRandom.current().nextDouble(100, 400), "measurand", "Energy.Active.Import.Register", "unit", "Wh")
            )
        );
        send("MeterValues", payload);
    }

    public void simulateFault(String faultCode) {
        send("StatusNotification", Map.of(
            "timestamp", Instant.now().toString(),
            "connectorStatus", "Faulted",
            "evseId", 1
        ));
        log.warn("[{}] Fault simulated: {}", chargePointId, faultCode);
    }

    private void send(String action, Map<String, Object> payload) {
        if (session == null || !session.isOpen()) return;
        try {
            // OCPP 2.0.1 message format: [2, uniqueId, action, payload]
            Object[] msg = {2, String.valueOf(messageId.getAndIncrement()), action, payload};
            String json = mapper.writeValueAsString(msg);
            session.sendMessage(new TextMessage(json));
            log.debug("[{}] Sent {}", chargePointId, action);
        } catch (IOException e) {
            log.error("[{}] Failed to send {}: {}", chargePointId, action, e.getMessage());
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        log.debug("[{}] Received: {}", chargePointId, message.getPayload());
        // Handle server-initiated commands (Reset, ChangeConfiguration, etc.)
        try {
            Object[] msg = mapper.readValue(message.getPayload(), Object[].class);
            if (msg.length >= 3 && msg[0].equals(2)) {
                String action = (String) msg[2];
                log.info("[{}] Server command: {}", chargePointId, action);
                // Respond with CallResult
                Object[] response = {3, msg[1], Map.of("status", "Accepted")};
                session.sendMessage(new TextMessage(mapper.writeValueAsString(response)));
            }
        } catch (Exception e) {
            log.error("[{}] Error handling message: {}", chargePointId, e.getMessage());
        }
    }

    public String getChargePointId() { return chargePointId; }
    public boolean isActiveTransaction() { return activeTransaction; }
}
