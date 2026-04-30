package com.evroaminghub.auth.service;

import com.evroaminghub.auth.dto.*;
import com.evroaminghub.auth.entity.RfidCard;
import com.evroaminghub.auth.entity.User;
import com.evroaminghub.auth.exception.DuplicateResourceException;
import com.evroaminghub.auth.exception.ResourceNotFoundException;
import com.evroaminghub.auth.repository.RfidCardRepository;
import com.evroaminghub.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RfidService {

    private final RfidCardRepository rfidCardRepository;
    private final UserRepository userRepository;

    @Value("${app.rfid.max-cards-per-user}")
    private int maxCardsPerUser;

    @Transactional(readOnly = true)
    public List<RfidCardResponse> getCardsByUser(UUID userId) {
        return rfidCardRepository.findByUserId(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public RfidCardResponse registerCard(UUID userId, RegisterRfidRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        long activeCards = rfidCardRepository.countByUserIdAndActiveTrue(userId);
        if (activeCards >= maxCardsPerUser) {
            throw new IllegalStateException("Maximum RFID cards (" + maxCardsPerUser + ") reached");
        }

        if (rfidCardRepository.existsByUid(request.getUid())) {
            throw new DuplicateResourceException("RFID card already registered: " + request.getUid());
        }

        RfidCard card = RfidCard.builder()
                .user(user)
                .uid(request.getUid().toUpperCase())
                .label(request.getLabel())
                .cardType(RfidCard.CardType.valueOf(request.getCardType() != null ? request.getCardType() : "RFID"))
                .active(true)
                .build();

        card = rfidCardRepository.save(card);
        log.info("RFID card {} registered for user {}", card.getUid(), userId);
        return toResponse(card);
    }

    @Transactional
    public void deactivateCard(UUID userId, UUID cardId) {
        RfidCard card = rfidCardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("RFID card not found"));

        if (!card.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("RFID card not found");
        }

        card.setActive(false);
        rfidCardRepository.save(card);
        log.info("RFID card {} deactivated", cardId);
    }

    @Transactional(readOnly = true)
    public RfidValidationResponse validateCard(String uid) {
        return rfidCardRepository.findByUidAndActiveTrue(uid.toUpperCase())
                .map(card -> RfidValidationResponse.builder()
                        .valid(true)
                        .userId(card.getUser().getId())
                        .cardId(card.getId())
                        .label(card.getLabel())
                        .build())
                .orElse(RfidValidationResponse.builder().valid(false).build());
    }

    private RfidCardResponse toResponse(RfidCard card) {
        return RfidCardResponse.builder()
                .id(card.getId())
                .uid(card.getUid())
                .label(card.getLabel())
                .cardType(card.getCardType().name())
                .active(card.isActive())
                .issuedAt(card.getIssuedAt())
                .expiresAt(card.getExpiresAt())
                .build();
    }
}
