package com.example.bankcards.controller;

import com.example.bankcards.dto.TransferRequestDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.TransferService;
import com.example.bankcards.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;
    private final UserService userService;

    @PostMapping
    @Operation(summary = "Перевод средств")
    public ResponseEntity<Void> transfer(Authentication authentication,
                                         @Valid @RequestBody TransferRequestDto request) {
        User currentUser = userService.getByUsername(authentication.getName());
        transferService.transferBetweenOwnCards(
                currentUser.getId(),
                request.getFromCardId(),
                request.getToCardId(),
                request.getAmount()
        );
        return ResponseEntity.ok().build();
    }
}

