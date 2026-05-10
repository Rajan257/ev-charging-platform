package com.evroaminghub.billing.repository;

import com.evroaminghub.billing.entity.Tariff;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface TariffRepository extends JpaRepository<Tariff, UUID> {}
