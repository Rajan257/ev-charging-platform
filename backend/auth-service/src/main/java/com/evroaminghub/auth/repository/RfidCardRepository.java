package com.evroaminghub.auth.repository;

import com.evroaminghub.auth.entity.RfidCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RfidCardRepository extends JpaRepository<RfidCard, UUID> {
    List<RfidCard> findByUserId(UUID userId);
    Optional<RfidCard> findByUidAndActiveTrue(String uid);
    boolean existsByUid(String uid);
    long countByUserIdAndActiveTrue(UUID userId);
}
