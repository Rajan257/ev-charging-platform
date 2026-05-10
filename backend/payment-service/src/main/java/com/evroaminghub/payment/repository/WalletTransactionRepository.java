package com.evroaminghub.payment.repository;

import com.evroaminghub.payment.entity.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, UUID> {}
