package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.repository.CardRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final CardRepository cardRepository;

    /**
     * Осуществляет перевод средств между двумя картами, принадлежащими одному владельцу.
     * Проверяет право собственности, состояние карты и наличие достаточного баланса.
     */
    @Transactional
    public void transferBetweenOwnCards(Long ownerId,
                                        Long fromCardId,
                                        Long toCardId,
                                        BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (fromCardId.equals(toCardId)) {
            throw new IllegalArgumentException("Source and target cards must be different");
        }

        Card from = cardRepository.findById(fromCardId)
                .orElseThrow(() -> new EntityNotFoundException("Source card not found"));
        Card to = cardRepository.findById(toCardId)
                .orElseThrow(() -> new EntityNotFoundException("Target card not found"));

        if (!from.getOwner().getId().equals(ownerId) || !to.getOwner().getId().equals(ownerId)) {
            throw new SecurityException("Transfers are allowed only between own cards");
        }

        validateCardForTransfer(from);
        validateCardForTransfer(to);

        if (from.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient funds on source card");
        }

        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));

        cardRepository.save(from);
        cardRepository.save(to);
    }

    private void validateCardForTransfer(Card card) {
        if (card.getStatus() != CardStatus.ACTIVE) {
            throw new IllegalStateException("Card is not active");
        }
    }
}

