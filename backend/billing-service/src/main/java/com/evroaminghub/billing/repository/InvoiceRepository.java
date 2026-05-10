package com.evroaminghub.billing.repository;

import com.evroaminghub.billing.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {}
