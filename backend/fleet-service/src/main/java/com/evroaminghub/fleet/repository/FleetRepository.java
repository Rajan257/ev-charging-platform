package com.evroaminghub.fleet.repository;
import com.evroaminghub.fleet.entity.Fleet;
import org.springframework.data.jpa.repository.JpaRepository;
public interface FleetRepository extends JpaRepository<Fleet, String> {}
