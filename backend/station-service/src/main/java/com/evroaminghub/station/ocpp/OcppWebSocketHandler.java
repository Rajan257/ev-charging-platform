package com.evroaminghub.station.ocpp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.evroaminghub.station.service.ChargePointService;
import com.evroaminghub.station.service.ConnectorStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * OCPP 2.0.1 JSON WebSocket Handler.
 * 
 * Handles OCPP messages from charge points:
 * - BootNotification
 * - Heartbeat
 * - StatusNotification
 * - TransactionEvent (Start/Update/End)
 * - Authorize
 * - MeterValues
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OcppWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final ChargePointService chargePointService;
    private final ConnectorStatusService connectorStatusService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    // chargePointId -> WebSocketSession
    private final Map<String, WebSocketSession> activeSessions = new ConcurrentHashMap<>();
    // sessionId -> chargePointId
    private final Map<String, String> sessionToChargePoint = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String chargePointId = extractChargePointId(session);
        log.info("OCPP connection established: chargePointId={}, sessionId={}", chargePointId, session.getId());
        activeSessions.put(chargePointId, session);
        sessionToChargePoint.put(session.getId(), chargePointId);
        chargePointService.updateConnectionStatus(chargePointId, "CONNECTED");
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String chargePointId = sessionToChargePoint.get(session.getId());
        String payload = message.getPayload();
        log.debug("OCPP message from {}: {}", chargePointId, payload);

        try {
            ArrayNode ocppMessage = (ArrayNode) objectMapper.readTree(payload);
            int messageType = ocppMessage.get(0).asInt();
            String messageId = ocppMessage.get(1).asText();

            if (messageType == 2) { // CALL
                String action = ocppMessage.get(2).asText();
                JsonNode callPayload = ocppMessage.get(3);
                handleCall(session, chargePointId, messageId, action, callPayload);
            } else if (messageType == 3) { // CALLRESULT
                // Handle response to our commands
                log.debug("Received CALLRESULT for messageId: {}", messageId);
            } else if (messageType == 4) { // CALLERROR
                log.warn("CALLERROR from {}: {}", chargePointId, payload);
            }
        } catch (Exception e) {
            log.error("Error processing OCPP message from {}: {}", chargePointId, e.getMessage());
            sendCallError(session, "unknown", "InternalError", e.getMessage());
        }
    }

    private void handleCall(WebSocketSession session, String chargePointId,
                            String messageId, String action, JsonNode payload) throws Exception {
        log.info("OCPP action={} from chargePoint={}", action, chargePointId);

        switch (action) {
            case "BootNotification" -> handleBootNotification(session, chargePointId, messageId, payload);
            case "Heartbeat"        -> handleHeartbeat(session, chargePointId, messageId);
            case "StatusNotification" -> handleStatusNotification(session, chargePointId, messageId, payload);
            case "TransactionEvent" -> handleTransactionEvent(session, chargePointId, messageId, payload);
            case "Authorize"        -> handleAuthorize(session, chargePointId, messageId, payload);
            case "MeterValues"      -> handleMeterValues(session, chargePointId, messageId, payload);
            default -> {
                log.warn("Unknown OCPP action: {}", action);
                sendCallError(session, messageId, "NotImplemented", "Action not implemented: " + action);
            }
        }
    }

    private void handleBootNotification(WebSocketSession session, String chargePointId,
                                        String messageId, JsonNode payload) throws Exception {
        String model   = payload.path("chargingStation").path("model").asText("Unknown");
        String vendor  = payload.path("chargingStation").path("vendorName").asText("Unknown");
        String firmware = payload.path("chargingStation").path("firmwareVersion").asText();

        chargePointService.onBootNotification(chargePointId, model, vendor, firmware);

        ObjectNode response = objectMapper.createObjectNode();
        response.put("currentTime", java.time.Instant.now().toString());
        response.put("interval", 300);
        response.put("status", "Accepted");

        sendCallResult(session, messageId, response);
        log.info("BootNotification accepted for chargePoint={}", chargePointId);
    }

    private void handleHeartbeat(WebSocketSession session, String chargePointId,
                                 String messageId) throws Exception {
        chargePointService.onHeartbeat(chargePointId);

        ObjectNode response = objectMapper.createObjectNode();
        response.put("currentTime", java.time.Instant.now().toString());
        sendCallResult(session, messageId, response);
    }

    private void handleStatusNotification(WebSocketSession session, String chargePointId,
                                          String messageId, JsonNode payload) throws Exception {
        String evseId     = payload.path("evseId").asText();
        String connectorId = payload.path("connectorId").asText();
        String status     = payload.path("connectorStatus").asText();
        String timestamp  = payload.path("timestamp").asText();

        connectorStatusService.updateStatus(chargePointId, evseId, connectorId, status, timestamp);

        // Publish to Kafka for real-time updates
        kafkaTemplate.send("connector.status.changed", chargePointId,
                Map.of("chargePointId", chargePointId, "evseId", evseId,
                        "connectorId", connectorId, "status", status, "timestamp", timestamp));

        sendCallResult(session, messageId, objectMapper.createObjectNode());
    }

    private void handleTransactionEvent(WebSocketSession session, String chargePointId,
                                        String messageId, JsonNode payload) throws Exception {
        String eventType   = payload.path("eventType").asText();     // Started, Updated, Ended
        String transactionId = payload.path("transactionInfo").path("transactionId").asText();
        String evseId      = payload.path("evse").path("id").asText();

        // Publish event to Kafka for session-service to consume
        kafkaTemplate.send("ocpp.transaction.event", transactionId,
                Map.of("eventType", eventType, "chargePointId", chargePointId,
                        "transactionId", transactionId, "evseId", evseId,
                        "payload", payload.toString()));

        ObjectNode response = objectMapper.createObjectNode();
        response.put("idTokenInfo", objectMapper.createObjectNode().put("status", "Accepted"));
        sendCallResult(session, messageId, response);
        log.info("TransactionEvent={} for tx={} from chargePoint={}", eventType, transactionId, chargePointId);
    }

    private void handleAuthorize(WebSocketSession session, String chargePointId,
                                 String messageId, JsonNode payload) throws Exception {
        String idToken = payload.path("idToken").path("idToken").asText();
        String tokenType = payload.path("idToken").path("type").asText("ISO14443");

        // Simple authorization - in production, call auth-service via REST
        ObjectNode idTokenInfo = objectMapper.createObjectNode();
        idTokenInfo.put("status", "Accepted");  // Always accept in demo

        ObjectNode response = objectMapper.createObjectNode();
        response.set("idTokenInfo", idTokenInfo);
        sendCallResult(session, messageId, response);
        log.info("Authorize idToken={} type={} from chargePoint={} -> Accepted", idToken, tokenType, chargePointId);
    }

    private void handleMeterValues(WebSocketSession session, String chargePointId,
                                   String messageId, JsonNode payload) throws Exception {
        String transactionId = payload.path("transactionId").asText();

        // Publish meter values to Kafka for session-service
        kafkaTemplate.send("ocpp.meter.values", transactionId,
                Map.of("chargePointId", chargePointId, "transactionId", transactionId,
                        "payload", payload.toString()));

        sendCallResult(session, messageId, objectMapper.createObjectNode());
    }

    // Send a remote command to a charge point
    public void sendRemoteStartTransaction(String chargePointId, String transactionId,
                                           String evseId, String idToken) throws Exception {
        WebSocketSession session = activeSessions.get(chargePointId);
        if (session == null || !session.isOpen()) {
            throw new IllegalStateException("Charge point not connected: " + chargePointId);
        }

        ArrayNode call = objectMapper.createArrayNode();
        call.add(2);
        call.add(transactionId);
        call.add("RequestStartTransaction");
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("evseId", Integer.parseInt(evseId));
        ObjectNode idTokenNode = objectMapper.createObjectNode();
        idTokenNode.put("idToken", idToken);
        idTokenNode.put("type", "ISO14443");
        payload.set("idToken", idTokenNode);
        payload.put("remoteStartId", 1);
        call.add(payload);

        session.sendMessage(new TextMessage(call.toString()));
        log.info("Sent RequestStartTransaction to chargePoint={}", chargePointId);
    }

    public void sendRemoteStopTransaction(String chargePointId, String transactionId) throws Exception {
        WebSocketSession session = activeSessions.get(chargePointId);
        if (session == null || !session.isOpen()) {
            throw new IllegalStateException("Charge point not connected: " + chargePointId);
        }

        ArrayNode call = objectMapper.createArrayNode();
        call.add(2);
        call.add(transactionId + "-stop");
        call.add("RequestStopTransaction");
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("transactionId", transactionId);
        call.add(payload);

        session.sendMessage(new TextMessage(call.toString()));
        log.info("Sent RequestStopTransaction to chargePoint={}", chargePointId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String chargePointId = sessionToChargePoint.remove(session.getId());
        if (chargePointId != null) {
            activeSessions.remove(chargePointId);
            chargePointService.updateConnectionStatus(chargePointId, "DISCONNECTED");
            log.info("OCPP connection closed: chargePointId={}, status={}", chargePointId, status);
        }
    }

    // --- Helpers ---
    private void sendCallResult(WebSocketSession session, String messageId, ObjectNode resultPayload) throws Exception {
        ArrayNode response = objectMapper.createArrayNode();
        response.add(3);
        response.add(messageId);
        response.add(resultPayload);
        session.sendMessage(new TextMessage(response.toString()));
    }

    private void sendCallError(WebSocketSession session, String messageId,
                               String errorCode, String errorDescription) throws Exception {
        ArrayNode error = objectMapper.createArrayNode();
        error.add(4);
        error.add(messageId);
        error.add(errorCode);
        error.add(errorDescription);
        error.add(objectMapper.createObjectNode());
        session.sendMessage(new TextMessage(error.toString()));
    }

    private String extractChargePointId(WebSocketSession session) {
        String path = session.getUri().getPath();
        String[] parts = path.split("/");
        return parts[parts.length - 1];  // /ocpp/{chargePointId}
    }

    public boolean isChargePointConnected(String chargePointId) {
        WebSocketSession session = activeSessions.get(chargePointId);
        return session != null && session.isOpen();
    }

    public int getConnectedCount() {
        return activeSessions.size();
    }
}
