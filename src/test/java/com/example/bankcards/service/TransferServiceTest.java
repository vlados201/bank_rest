package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class TransferServiceTest {

    private CardRepository cardRepository;
    private TransferService transferService;

    private Card from;
    private Card to;
    private final Long ownerId = 1L;

    @BeforeEach
    void setUp() {

        cardRepository = mock(CardRepository.class);
        transferService = new TransferService(cardRepository);

        User owner = new User();
        owner.setId(ownerId);

        from = new Card();
        from.setId(1L);
        from.setOwner(owner);
        from.setStatus(CardStatus.ACTIVE);
        from.setBalance(new BigDecimal("100.00"));

        to = new Card();
        to.setId(2L);
        to.setOwner(owner);
        to.setStatus(CardStatus.ACTIVE);
        to.setBalance(new BigDecimal("50.00"));
    }

    @Test
    void transferBetweenOwnCards_ShouldMoveFunds() {

        when(cardRepository.findById(1L)).thenReturn(Optional.of(from));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(to));

        transferService.transferBetweenOwnCards(ownerId,1L,2L,new BigDecimal("30.00"));

        assertThat(from.getBalance()).isEqualTo(new BigDecimal("70.00"));
        assertThat(to.getBalance()).isEqualTo(new BigDecimal("80.00"));

        verify(cardRepository,times(2)).save(any(Card.class));
    }

    @Test
    void transfer_ShouldThrow_WhenAmountNull() {

        assertThrows(IllegalArgumentException.class, () ->
                transferService.transferBetweenOwnCards(ownerId,1L,2L,null)
        );
    }

    @Test
    void transfer_ShouldThrow_WhenAmountNegative() {

        assertThrows(IllegalArgumentException.class, () ->
                transferService.transferBetweenOwnCards(ownerId,1L,2L,new BigDecimal("-10"))
        );
    }

    @Test
    void transfer_ShouldThrow_WhenSameCard() {

        assertThrows(IllegalArgumentException.class, () ->
                transferService.transferBetweenOwnCards(ownerId,1L,1L,new BigDecimal("10"))
        );
    }

    @Test
    void transfer_ShouldThrow_WhenSourceCardNotFound() {

        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                transferService.transferBetweenOwnCards(ownerId,1L,2L,new BigDecimal("10"))
        );
    }

    @Test
    void transfer_ShouldThrow_WhenTargetCardNotFound() {

        when(cardRepository.findById(1L)).thenReturn(Optional.of(from));
        when(cardRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                transferService.transferBetweenOwnCards(ownerId,1L,2L,new BigDecimal("10"))
        );
    }

    @Test
    void transfer_ShouldThrow_WhenCardNotOwnedByUser() {

        User anotherUser = new User();
        anotherUser.setId(2L);

        to.setOwner(anotherUser);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(from));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(to));

        assertThrows(SecurityException.class, () ->
                transferService.transferBetweenOwnCards(ownerId,1L,2L,new BigDecimal("10"))
        );
    }

    @Test
    void transfer_ShouldThrow_WhenFromCardNotActive() {

        from.setStatus(CardStatus.BLOCKED);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(from));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(to));

        assertThrows(IllegalStateException.class, () ->
                transferService.transferBetweenOwnCards(ownerId,1L,2L,new BigDecimal("10"))
        );
    }

    @Test
    void transfer_ShouldThrow_WhenToCardNotActive() {

        to.setStatus(CardStatus.BLOCKED);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(from));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(to));

        assertThrows(IllegalStateException.class, () ->
                transferService.transferBetweenOwnCards(ownerId,1L,2L,new BigDecimal("10"))
        );
    }

    @Test
    void transfer_ShouldThrow_WhenInsufficientFunds() {

        from.setBalance(new BigDecimal("10"));

        when(cardRepository.findById(1L)).thenReturn(Optional.of(from));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(to));

        assertThrows(IllegalStateException.class, () ->
                transferService.transferBetweenOwnCards(ownerId,1L,2L,new BigDecimal("50"))
        );
    }
}