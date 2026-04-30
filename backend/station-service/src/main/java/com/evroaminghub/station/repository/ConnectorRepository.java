package com.evroaminghub.station.repository;

import com.evroaminghub.station.entity.Connector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConnectorRepository extends JpaRepository<Connector, UUID> {
    List<Connector> findByStationId(UUID stationId);
    Optional<Connector> findByEvseId(String evseId);
}
