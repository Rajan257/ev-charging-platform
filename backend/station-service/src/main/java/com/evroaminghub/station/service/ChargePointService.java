package com.evroaminghub.station.service;

import com.evroaminghub.station.entity.ChargePoint;
import com.evroaminghub.station.repository.ChargePointRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChargePointService {

    private final ChargePointRepository chargePointRepository;

    @Transactional
    public void updateConnectionStatus(String chargePointId, String status) {
        chargePointRepository.findByChargePointId(chargePointId).ifPresent(cp -> {
            cp.setConnectionStatus(status);
            if ("CONNECTED".equals(status)) cp.setLastBootAt(Instant.now());
            chargePointRepository.save(cp);
        });
    }

    @Transactional
    public void onBootNotification(String chargePointId, String model, String vendor, String firmware) {
        ChargePoint cp = chargePointRepository.findByChargePointId(chargePointId)
                .orElse(ChargePoint.builder().chargePointId(chargePointId).build());
        cp.setModel(model);
        cp.setVendor(vendor);
        cp.setFirmwareVersion(firmware);
        cp.setConnectionStatus("CONNECTED");
        cp.setLastBootAt(Instant.now());
        chargePointRepository.save(cp);
        log.info("Charge point booted: {} model={} vendor={}", chargePointId, model, vendor);
    }

    @Transactional
    public void onHeartbeat(String chargePointId) {
        chargePointRepository.findByChargePointId(chargePointId).ifPresent(cp -> {
            cp.setLastHeartbeatAt(Instant.now());
            chargePointRepository.save(cp);
        });
    }
}
