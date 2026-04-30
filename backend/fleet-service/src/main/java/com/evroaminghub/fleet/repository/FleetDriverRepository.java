package com.evroaminghub.fleet.repository;
import com.evroaminghub.fleet.entity.FleetDriver;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface FleetDriverRepository extends JpaRepository<FleetDriver, String> {
    List<FleetDriver> findByFleetId(String fleetId);
}
