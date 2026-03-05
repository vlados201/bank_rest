package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CreateCardRequestDto;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;
    private final UserService userService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/cards")
    @Operation(summary = "Создать карту")
    public ResponseEntity<CardDto> createCard(@Valid @RequestBody CreateCardRequestDto request) {
        CardDto created = cardService.create(request);
        return ResponseEntity.ok(created);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/admin/cards/{id}/block")
    @Operation(summary = "Заблокировать карту по id")
    public ResponseEntity<Void> blockCard(
            @PathVariable @Parameter(name = "id", description = "id карты") Long id) {
        cardService.changeStatus(id, CardStatus.BLOCKED);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/admin/cards/{id}/activate")
    @Operation(summary = "Активировать карту по id")
    public ResponseEntity<Void> activateCard(
            @PathVariable @Parameter(name = "id", description = "id карты") Long id) {
        cardService.changeStatus(id, CardStatus.ACTIVE);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/admin/cards/{id}")
    @Operation(summary = "Удалить карту по id")
    public ResponseEntity<Void> deleteCard(
            @PathVariable @Parameter(name = "id", description = "id карты") Long id) {
        cardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/cards")
    @Operation(summary = "Посмотреть список всех зарегестрированных карт с возможностью пагинации и фильтрации")
    public ResponseEntity<Page<CardDto>> getAllCards(
            @RequestParam(defaultValue = "0") @Parameter(name = "page", description = "№ страницы") int page,
            @RequestParam(defaultValue = "20") @Parameter(name = "size", description = "Кол-во записей на одной странице") int size,
            @RequestParam(required = false) @Parameter(name = "ownerId", description = "id держателя карты") Long ownerId,
            @RequestParam(required = false) @Parameter(name = "status", description = "Статус карты") CardStatus status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"));
        Page<CardDto> result = cardService.getAllCards(ownerId, status, pageable);
        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/cards/{id}")
    @Operation(summary = "Посмотреть информацию о карте по id")
    public ResponseEntity<CardDto> getCardById(
            @PathVariable @Parameter(name = "id", description = "id карты") Long id) {
        CardDto dto = cardService.getById(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/cards")
    @Operation(summary = "Посмотреть список своих карт (в т. ч. узнать баланс) с возможностью пагинации и фильтрации")
    public ResponseEntity<Page<CardDto>> getMyCards(Authentication authentication,
            @RequestParam(defaultValue = "0") @Parameter(name = "page", description = "№ страницы") int page,
            @RequestParam(defaultValue = "20") @Parameter(name = "size", description = "Кол-во записей на одной странице") int size,
            @RequestParam(required = false) @Parameter(name = "status", description = "Статус карты") CardStatus status) {
        User currentUser = userService.getByUsername(authentication.getName());
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"));
        Page<CardDto> result = cardService.getCardsForOwner(currentUser.getId(), status, pageable);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/cards/{id}/block-request")
    @Operation(summary = "Сделать запрос на блокировку карты")
    public ResponseEntity<Void> requestBlock(Authentication authentication,
            @PathVariable @Parameter(name = "id", description = "id карты") Long id) {
        User currentUser = userService.getByUsername(authentication.getName());
        cardService.requestBlockByOwner(currentUser.getId(), id);
        return ResponseEntity.ok().build();
    }
}

