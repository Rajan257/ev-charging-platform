package com.evroaminghub.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

// Wallet repository stub - wallet creation is handled by payment-service
// This is a placeholder so UserService can compile without cross-service dependency
@Repository
public interface WalletRepository extends org.springframework.data.repository.Repository<Object, UUID> {
    // No-op stub for compilation. Actual wallet operations are in payment-service.
}
