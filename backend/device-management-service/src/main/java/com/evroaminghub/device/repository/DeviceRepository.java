package com.evroaminghub.device.repository;

import com.evroaminghub.device.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.List;

@Repository
public interface DeviceRepository extends JpaRepository<Device, String> {
    List<Device> findByStationId(String stationId);
    List<Device> findByStatus(Device.DeviceStatus status);
    List<Device> findByLastHeartbeatBefore(Instant threshold);
}
