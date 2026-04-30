package com.evroaminghub.station.repository;

import com.evroaminghub.station.entity.ChargingStation;
import com.evroaminghub.station.entity.StationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChargingStationRepository extends JpaRepository<ChargingStation, UUID> {

    Page<ChargingStation> findByCityIgnoreCase(String city, Pageable pageable);
    Page<ChargingStation> findByStatus(StationStatus status, Pageable pageable);
    List<ChargingStation> findByCpoNetworkId(UUID cpoNetworkId);
    long countByStatus(StationStatus status);

    @Query("SELECT s FROM ChargingStation s WHERE " +
           "s.latitude BETWEEN :minLat AND :maxLat AND " +
           "s.longitude BETWEEN :minLng AND :maxLng")
    Page<ChargingStation> findByLocationBBox(
            @Param("minLat") double minLat, @Param("maxLat") double maxLat,
            @Param("minLng") double minLng, @Param("maxLng") double maxLng,
            Pageable pageable);
}
