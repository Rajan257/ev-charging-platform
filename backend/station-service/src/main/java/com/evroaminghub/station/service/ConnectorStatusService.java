package com.evroaminghub.station.service;

import com.evroaminghub.station.entity.Connector;
import com.evroaminghub.station.entity.ConnectorStatus;
import com.evroaminghub.station.repository.ConnectorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConnectorStatusService {

    private final ConnectorRepository connectorRepository;

    @Transactional
    public void updateStatus(String chargePointId, String evseId, String connectorId, String status, String timestamp) {
        // Map EVSE ID to our connector via evse_id field
        connectorRepository.findByEvseId(evseId).ifPresentOrElse(
                connector -> {
                    try {
                        connector.setStatus(ConnectorStatus.valueOf(status.toUpperCase().replace(" ", "_")));
                        connector.setLastStatusUpdate(Instant.now());
                        connectorRepository.save(connector);
                        log.info("Connector {} status updated to {}", evseId, status);
                    } catch (IllegalArgumentException e) {
                        log.warn("Unknown connector status: {}", status);
                    }
                },
                () -> log.warn("Unknown EVSE ID: {} from chargePoint: {}", evseId, chargePointId)
        );
    }
}
