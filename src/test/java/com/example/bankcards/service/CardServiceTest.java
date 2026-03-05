package com.example.bankcards.service;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CreateCardRequestDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CardServiceTest {

    private CardRepository cardRepository;
    private UserRepository userRepository;
    private CryptoService cryptoService;

    private CardService cardService;

    private Card testCard;
    private User testUser;

    @BeforeEach
    void setUp() {
        cardRepository = mock(CardRepository.class);
        userRepository = mock(UserRepository.class);
        cryptoService = mock(CryptoService.class);
        cardService = new CardService(cardRepository, userRepository, cryptoService);

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("Иван");

        testCard = new Card();
        testCard.setId(10L);
        testCard.setOwner(testUser);
        testCard.setMaskedNumber("**** **** **** 1234");
        testCard.setEncryptedNumber("encrypted");
        testCard.setExpiryDate(LocalDate.now().plusYears(3));
        testCard.setStatus(CardStatus.ACTIVE);
        testCard.setBalance(BigDecimal.valueOf(1000.00));
    }

    @Test
    void create_ShouldCreateActiveCardWithZeroBalance() {
        CreateCardRequestDto request = new CreateCardRequestDto();
        request.setOwnerId(1L);

        User owner = new User();
        owner.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(cryptoService.encrypt(any())).thenReturn("encrypted");

        ArgumentCaptor<Card> cardCaptor = ArgumentCaptor.forClass(Card.class);
        when(cardRepository.save(cardCaptor.capture())).thenAnswer(invocation -> {
            Card c = cardCaptor.getValue();
            c.setId(10L);
            return c;
        });

        CardDto result = cardService.create(request);

        Card saved = cardCaptor.getValue();
        assertThat(saved.getOwner().getId()).isEqualTo(1L);
        assertThat(saved.getStatus()).isEqualTo(CardStatus.ACTIVE);
        assertThat(saved.getBalance()).isEqualTo(BigDecimal.ZERO);
        assertThat(saved.getExpiryDate()).isAfter(LocalDate.now());
        assertThat(saved.getEncryptedNumber()).isEqualTo("encrypted");
        assertThat(saved.getMaskedNumber()).startsWith("**** **** **** ");

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getOwnerId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(CardStatus.ACTIVE.name());
    }

    @Test
    void changeStatus() {
        Long cardId = 10L;
        CardStatus status = CardStatus.BLOCKED;
        ArgumentCaptor<Card> cardCaptor = ArgumentCaptor.forClass(Card.class);
        when(cardRepository.save(cardCaptor.capture())).thenAnswer(invocation -> {
            Card c = cardCaptor.getValue();
            return c;
        });
        when(cardRepository.findById(cardId)).thenReturn(Optional.ofNullable(testCard));
        cardService.changeStatus(cardId, status);
        Card changedCard = cardCaptor.getValue();
        assertThat(changedCard.getStatus().equals(CardStatus.BLOCKED));
    }

    @Test
    void changeStatus_ShouldThrowException_WhenCardNotFound() {

        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> cardService.changeStatus(1L, CardStatus.BLOCKED)
        );

        verify(cardRepository, never()).save(any());
    }

    @Test
    void deleteCard_ShouldDelete_WhenCardExists() {

        when(cardRepository.existsById(10L)).thenReturn(true);
        cardService.deleteCard(10L);

        verify(cardRepository).deleteById(10L);
    }

    @Test
    void deleteCard_ShouldThrowException_WhenCardNotFound() {
        when(cardRepository.existsById(10L)).thenReturn(false);
        assertThrows(
                jakarta.persistence.EntityNotFoundException.class,
                () -> cardService.deleteCard(10L)
        );
        verify(cardRepository, never()).deleteById(any());
    }

    @Test
    void requestBlockByOwner_ShouldSetStatusBlockRequested() {

        when(cardRepository.findById(10L)).thenReturn(Optional.of(testCard));
        cardService.requestBlockByOwner(1L, 10L);
        ArgumentCaptor<Card> captor = ArgumentCaptor.forClass(Card.class);
        verify(cardRepository).save(captor.capture());
        Card saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(CardStatus.BLOCK_REQUESTED);
    }

    @Test
    void requestBlockByOwner_ShouldThrowSecurityException_WhenNotOwner() {
        testUser.setId(2L);
        when(cardRepository.findById(10L)).thenReturn(Optional.of(testCard));
        assertThrows(
                SecurityException.class,
                () -> cardService.requestBlockByOwner(1L, 10L)
        );
        verify(cardRepository, never()).save(any());
    }

    @Test
    void getById_ShouldReturnDto_WhenCardExists() {
        when(cardRepository.findById(10L)).thenReturn(Optional.of(testCard));
        CardDto dto = cardService.getById(10L);
        assertThat(dto.getId()).isEqualTo(10L);
        assertThat(dto.getOwnerId()).isEqualTo(1L);
        assertThat(dto.getMaskedNumber()).isEqualTo("**** **** **** 1234");
        assertThat(dto.getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    void getCardsForOwner_ShouldReturnPage() {
        Page<Card> page = new PageImpl<>(List.of(testCard));
        when(cardRepository.findByOwnerId(eq(1L), any())).thenReturn(page);
        Page<CardDto> result = cardService.getCardsForOwner(1L, null,
                PageRequest.of(0,10));
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getOwnerId()).isEqualTo(1L);
    }

    @Test
    void getCardsForOwner_ShouldFilterByStatus() {
        Page<Card> page = new PageImpl<>(List.of(testCard));
        when(cardRepository.findByOwnerIdAndStatus(eq(1L), eq(CardStatus.ACTIVE), any()))
                .thenReturn(page);
        Page<CardDto> result =
                cardService.getCardsForOwner(
                        1L,
                        CardStatus.ACTIVE,
                        PageRequest.of(0,10)
                );
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void getAllCards_ShouldReturnAllCards() {

        Page<Card> page = new PageImpl<>(java.util.List.of(testCard));
        when(cardRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<CardDto> result = cardService.getAllCards(null,null,
                        PageRequest.of(0,10));

        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void expireCards_ShouldCallRepositoryWithCurrentDate() {

        when(cardRepository.expireCards(any())).thenReturn(3);
        cardService.expireCards();

        verify(cardRepository, times(1))
                .expireCards(any(LocalDate.class));
    }
}

