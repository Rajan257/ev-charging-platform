package com.evroaminghub.fraud.repository;

import com.evroaminghub.fraud.entity.FraudAlert;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FraudAlertRepository extends JpaRepository<FraudAlert, String> {
    List<FraudAlert> findBySessionId(String sessionId);
    Page<FraudAlert> findBySeverity(FraudAlert.AlertSeverity severity, Pageable pageable);
    long countByStatus(FraudAlert.AlertStatus status);
    long countBySeverityAndStatus(FraudAlert.AlertSeverity severity, FraudAlert.AlertStatus status);
}
