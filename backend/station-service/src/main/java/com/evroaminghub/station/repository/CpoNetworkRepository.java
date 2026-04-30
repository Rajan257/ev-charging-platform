package com.evroaminghub.station.repository;

import com.evroaminghub.station.entity.CpoNetwork;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CpoNetworkRepository extends JpaRepository<CpoNetwork, UUID> {
    Optional<CpoNetwork> findByCode(String code);
}
