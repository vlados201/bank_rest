package com.example.bankcards.controller;

import com.example.bankcards.dto.UserDto;
import com.example.bankcards.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "Посмотреть список всех пользователей")
    public ResponseEntity<Page<UserDto>> getUsers(
            @RequestParam(defaultValue = "0") @Parameter(name = "page", description = "№ страницы") int page,
            @RequestParam(defaultValue = "20") @Parameter(name = "size", description = "Кол-во отображаемых записей") int size,
            @RequestParam(required = false) @Parameter(name = "username", description = "Имя пользователя") String username,
            @RequestParam(required = false) @Parameter(name = "enabled", description = "Флаг активности аккаунта") Boolean enabled) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserDto> users = userService.getUsers(username, enabled, pageable);
        return ResponseEntity.ok(users);
    }

    @PatchMapping("/{id}/enabled")
    @Operation(summary = "Изменить статус учетной записи пользователя")
    public ResponseEntity<Void> setUserEnabled(
            @PathVariable @Parameter(name = "id", description = "id пользователя") Long id,
            @RequestParam @Parameter(name = "enabled", description = "Флаг активности аккаунта") boolean enabled) {
        userService.setUserEnabled(id, enabled);
        return ResponseEntity.ok().build();
    }
}

