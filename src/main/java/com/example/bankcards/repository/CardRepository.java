package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;

public interface CardRepository extends JpaRepository<Card, Long> {
    Page<Card> findByOwnerId(Long ownerId, Pageable pageable);

    Page<Card> findByOwnerIdAndStatus(Long ownerId, CardStatus status, Pageable pageable);

    Page<Card> findByStatus(CardStatus status, Pageable pageable);

    @Modifying
    @Query("""
            UPDATE Card c
            SET c.status = 'EXPIRED'
            WHERE c.expiryDate < :today
            AND c.status <> 'EXPIRED'
            """)
    int expireCards(LocalDate today);
}