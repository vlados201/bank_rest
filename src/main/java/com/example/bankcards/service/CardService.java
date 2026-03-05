package com.example.bankcards.service;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CreateCardRequestDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CardMaskingUtil;
import com.example.bankcards.util.CardNumberGenerator;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class CardService {
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final CryptoService cryptoService;

    /**
     * Метод создает новую карту для пользователя.
     * Генерирует номер карты и шифроует его,
     * хранит замаскированное представление и устанавливает начальное состояние.
     */
    @Transactional
    public CardDto create(CreateCardRequestDto dto) {
        User owner = userRepository.findById(dto.getOwnerId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        String rawNumber = CardNumberGenerator.generate();
        String masked = CardMaskingUtil.maskCardNumber(rawNumber);
        String encrypted = cryptoService.encrypt(rawNumber);

        Card card = new Card();
        card.setOwner(owner);
        card.setEncryptedNumber(encrypted);
        card.setMaskedNumber(masked);
        card.setExpiryDate(LocalDate.now().plusYears(3));
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(BigDecimal.ZERO);

        Card saved = cardRepository.save(card);
        return toDto(saved);
    }

    /**
     * Меняет статус карты (ACTIVE, BLOCKED, EXPIRED, BLOCK_REQUESTED).
     */
    @Transactional
    public void changeStatus(Long cardId, CardStatus status) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new EntityNotFoundException("Card not found"));
        card.setStatus(status);
        cardRepository.save(card);
    }

    /**
     * Метод удаляет карту по id.
     */
    @Transactional
    public void deleteCard(Long cardId) {
        if (!cardRepository.existsById(cardId)) {
            throw new EntityNotFoundException("Card not found");
        }
        cardRepository.deleteById(cardId);
    }

    /**
     * Метод реализовывает возможность пользователю делать запрос
     * на блакировку карты.
     */
    @Transactional
    public void requestBlockByOwner(Long ownerId, Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new EntityNotFoundException("Card not found"));
        if (!card.getOwner().getId().equals(ownerId)) {
            throw new SecurityException("You can request block only for your own card");
        }
        card.setStatus(CardStatus.BLOCK_REQUESTED);
        cardRepository.save(card);
    }

    /**
     * Возвращает постраничный список карт для конкретного
     * владельца с возможностью фильтрации по статусу.
     */
    @Transactional
    public Page<CardDto> getCardsForOwner(Long ownerId, CardStatus status, Pageable pageable) {
        Page<Card> page;
        if (status != null) {
            page = cardRepository.findByOwnerIdAndStatus(ownerId, status, pageable);
        } else {
            page = cardRepository.findByOwnerId(ownerId, pageable);
        }
        return page.map(this::toDto);
    }

    /**
     * Возвращает постраничный список карточек для
     * администратора с возможностью фильтрации по владельцу и статусу.
     */
    @Transactional
    public Page<CardDto> getAllCards(Long ownerId, CardStatus status, Pageable pageable) {
        Page<Card> page;
        if (ownerId != null && status != null) {
            page = cardRepository.findByOwnerIdAndStatus(ownerId, status, pageable);
        } else if (ownerId != null) {
            page = cardRepository.findByOwnerId(ownerId, pageable);
        } else if (status != null) {
            page = cardRepository.findByStatus(status, pageable);
        } else {
            page = cardRepository.findAll(pageable);
        }
        return page.map(this::toDto);
    }

    /**
     * Возвращает карту по идентификатору.
     */
    @Transactional
    public CardDto getById(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new EntityNotFoundException("Card not found"));
        return toDto(card);
    }

    /**
     * Проверяет истек ли срок действия карты.
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void expireCards() {
        cardRepository.expireCards(LocalDate.now());
    }

    private CardDto toDto(Card card) {
        CardDto dto = new CardDto();
        dto.setId(card.getId());
        dto.setMaskedNumber(card.getMaskedNumber());
        dto.setOwnerId(card.getOwner().getId());
        dto.setExpiryDate(card.getExpiryDate());
        dto.setStatus(card.getStatus().name());
        dto.setBalance(card.getBalance());
        return dto;
    }


}
