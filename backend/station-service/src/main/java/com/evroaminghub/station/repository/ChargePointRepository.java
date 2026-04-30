package com.evroaminghub.station.repository;

import com.evroaminghub.station.entity.ChargePoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChargePointRepository extends JpaRepository<ChargePoint, UUID> {
    Optional<ChargePoint> findByChargePointId(String chargePointId);
}
