package com.evroaminghub.device.service;

import com.evroaminghub.device.dto.*;
import com.evroaminghub.device.entity.Device;
import com.evroaminghub.device.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RedisTemplate<String, Object> redisTemplate;

    public List<DeviceResponse> getDevices(String stationId, String status) {
        List<Device> devices;
        if (stationId != null) {
            devices = deviceRepository.findByStationId(stationId);
        } else if (status != null) {
            devices = deviceRepository.findByStatus(Device.DeviceStatus.valueOf(status));
        } else {
            devices = deviceRepository.findAll();
        }
        return devices.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public DeviceResponse getDevice(String id) {
        return deviceRepository.findById(id)
            .map(this::toResponse)
            .orElseThrow(() -> new RuntimeException("Device not found: " + id));
    }

    public DeviceHealthResponse getDeviceHealth(String id) {
        Device device = deviceRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Device not found: " + id));

        // Pull latest health from Redis cache
        String cacheKey = "device:health:" + id;
        @SuppressWarnings("unchecked")
        Map<String, Object> healthData = (Map<String, Object>) redisTemplate.opsForValue().get(cacheKey);

        boolean isOnline = device.getLastHeartbeat() != null
            && Instant.now().minusSeconds(120).isBefore(device.getLastHeartbeat());

        return DeviceHealthResponse.builder()
            .deviceId(id)
            .chargePointId(device.getChargePointId())
            .status(device.getStatus().name())
            .isOnline(isOnline)
            .lastHeartbeat(device.getLastHeartbeat())
            .firmwareVersion(device.getFirmwareVersion())
            .certificateValid(device.getCertificateValid())
            .certificateExpiry(device.getCertificateExpiry())
            .additionalMetrics(healthData != null ? healthData : new HashMap<>())
            .build();
    }

    public void restartDevice(String id, String resetType) {
        Device device = deviceRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Device not found: " + id));

        log.info("Sending Reset command ({}) to device {}", resetType, device.getChargePointId());

        // Publish OCPP command via Kafka — station-service WebSocket handler consumes this
        Map<String, Object> command = new HashMap<>();
        command.put("chargePointId", device.getChargePointId());
        command.put("command", "Reset");
        command.put("resetType", resetType);
        command.put("timestamp", Instant.now().toString());

        kafkaTemplate.send("charger.command", device.getChargePointId(), command);

        // Update status
        device.setStatus(Device.DeviceStatus.BOOTING);
        deviceRepository.save(device);
    }

    public void unlockConnector(String id, int connectorId) {
        Device device = deviceRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Device not found: " + id));

        Map<String, Object> command = new HashMap<>();
        command.put("chargePointId", device.getChargePointId());
        command.put("command", "UnlockConnector");
        command.put("connectorId", connectorId);
        command.put("timestamp", Instant.now().toString());

        kafkaTemplate.send("charger.command", device.getChargePointId(), command);
        log.info("Sent UnlockConnector for connector {} on device {}", connectorId, device.getChargePointId());
    }

    public void updateConfiguration(String id, String key, String value) {
        Device device = deviceRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Device not found: " + id));

        Map<String, Object> command = new HashMap<>();
        command.put("chargePointId", device.getChargePointId());
        command.put("command", "ChangeConfiguration");
        command.put("key", key);
        command.put("value", value);

        kafkaTemplate.send("charger.command", device.getChargePointId(), command);
        log.info("Sent ChangeConfiguration {} = {} to device {}", key, value, device.getChargePointId());
    }

    public void triggerFirmwareUpdate(String id, FirmwareUpdateRequest request) {
        Device device = deviceRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Device not found: " + id));

        Map<String, Object> command = new HashMap<>();
        command.put("chargePointId", device.getChargePointId());
        command.put("command", "UpdateFirmware");
        command.put("firmwareUrl", request.getFirmwareUrl());
        command.put("retrieveDate", request.getRetrieveDate());
        command.put("targetVersion", request.getTargetVersion());

        device.setStatus(Device.DeviceStatus.UPDATING);
        deviceRepository.save(device);

        kafkaTemplate.send("charger.command", device.getChargePointId(), command);
        kafkaTemplate.send("charger.firmware.update", device.getChargePointId(), command);
        log.info("Firmware update triggered for device {}", device.getChargePointId());
    }

    public DiagnosticsResponse getDiagnostics(String id) {
        Device device = deviceRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Device not found: " + id));

        return DiagnosticsResponse.builder()
            .deviceId(id)
            .chargePointId(device.getChargePointId())
            .manufacturer(device.getManufacturer())
            .model(device.getModel())
            .serialNumber(device.getSerialNumber())
            .firmwareVersion(device.getFirmwareVersion())
            .iccid(device.getIccid())
            .imsi(device.getImsi())
            .lastBootNotification(device.getLastBootNotification())
            .lastHeartbeat(device.getLastHeartbeat())
            .status(device.getStatus().name())
            .build();
    }

    public Map<String, Object> getConfiguration(String id, List<String> keys) {
        Device device = deviceRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Device not found: " + id));

        Map<String, Object> command = new HashMap<>();
        command.put("chargePointId", device.getChargePointId());
        command.put("command", "GetConfiguration");
        command.put("keys", keys);
        kafkaTemplate.send("charger.command", device.getChargePointId(), command);

        return Map.of("status", "COMMAND_SENT", "chargePointId", device.getChargePointId());
    }

    public DeviceResponse registerDevice(RegisterDeviceRequest request) {
        Device device = Device.builder()
            .chargePointId(request.getChargePointId())
            .stationId(request.getStationId())
            .manufacturer(request.getManufacturer())
            .model(request.getModel())
            .serialNumber(request.getSerialNumber())
            .firmwareVersion(request.getFirmwareVersion())
            .iccid(request.getIccid())
            .imsi(request.getImsi())
            .status(Device.DeviceStatus.ONLINE)
            .lastBootNotification(Instant.now())
            .lastHeartbeat(Instant.now())
            .build();

        Device saved = deviceRepository.save(device);
        log.info("Device registered: {}", saved.getChargePointId());
        kafkaTemplate.send("charger.status.updated", saved.getChargePointId(),
            Map.of("chargePointId", saved.getChargePointId(), "status", "ONLINE", "event", "BootNotification"));
        return toResponse(saved);
    }

    // Scheduled health check every 5 minutes
    @Scheduled(fixedRate = 300_000)
    public void checkDeviceHealth() {
        Instant threshold = Instant.now().minusSeconds(120);
        List<Device> staleDevices = deviceRepository.findByLastHeartbeatBefore(threshold);
        staleDevices.forEach(device -> {
            if (device.getStatus() == Device.DeviceStatus.ONLINE) {
                log.warn("Device {} missed heartbeat — marking OFFLINE", device.getChargePointId());
                device.setStatus(Device.DeviceStatus.OFFLINE);
                deviceRepository.save(device);
                kafkaTemplate.send("charger.health.alert", device.getChargePointId(),
                    Map.of("chargePointId", device.getChargePointId(), "alert", "HEARTBEAT_MISSED",
                           "lastHeartbeat", device.getLastHeartbeat().toString()));
            }
        });
    }

    private DeviceResponse toResponse(Device d) {
        return DeviceResponse.builder()
            .id(d.getId())
            .chargePointId(d.getChargePointId())
            .stationId(d.getStationId())
            .status(d.getStatus().name())
            .firmwareVersion(d.getFirmwareVersion())
            .manufacturer(d.getManufacturer())
            .model(d.getModel())
            .lastHeartbeat(d.getLastHeartbeat())
            .certificateValid(d.getCertificateValid())
            .build();
    }
}
