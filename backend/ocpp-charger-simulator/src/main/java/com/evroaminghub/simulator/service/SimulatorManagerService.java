package com.evroaminghub.simulator.service;

import com.evroaminghub.simulator.simulator.SimulatedChargePoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class SimulatorManagerService {

    private final Map<String, SimulatedChargePoint> chargers = new ConcurrentHashMap<>();

    public void startSimulation(int count, String wsUrl) {
        for (int i = 1; i <= count; i++) {
            String id = String.format("SIM-CP-%04d", i);
            if (!chargers.containsKey(id)) {
                SimulatedChargePoint cp = new SimulatedChargePoint(id, wsUrl);
                try {
                    cp.connect();
                    chargers.put(id, cp);
                    log.info("Started simulated charger: {}", id);
                } catch (Exception e) {
                    log.error("Failed to connect charger {}: {}", id, e.getMessage());
                }
            }
        }
    }

    public void stopAll() {
        chargers.clear();
        log.info("All simulated chargers stopped");
    }

    public void startTransaction(String chargePointId, String idToken) {
        SimulatedChargePoint cp = chargers.get(chargePointId);
        if (cp != null) cp.startTransaction(idToken);
        else log.warn("Charger not found: {}", chargePointId);
    }

    public void stopTransaction(String chargePointId, String reason) {
        SimulatedChargePoint cp = chargers.get(chargePointId);
        if (cp != null) cp.stopTransaction(reason);
    }

    public void simulateFault(String chargePointId, String faultCode) {
        SimulatedChargePoint cp = chargers.get(chargePointId);
        if (cp != null) cp.simulateFault(faultCode);
    }

    public Map<String, Object> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("totalChargers", chargers.size());
        status.put("chargers", chargers.entrySet().stream()
            .map(e -> Map.of(
                "id", e.getKey(),
                "activeTransaction", e.getValue().isActiveTransaction()
            )).toList());
        return status;
    }
}
