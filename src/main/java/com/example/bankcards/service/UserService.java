package com.example.bankcards.service;

import com.example.bankcards.dto.RegisterUserRequestDto;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Регистрирует нового пользователя приложения с ролью ROLE_USER.
     */
    @Transactional
    public User registerUser(RegisterUserRequestDto request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username is already taken");
        }

        Role userRole = roleRepository.findAll().stream()
                .filter(r -> "ROLE_USER".equals(r.getName()))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Default role ROLE_USER not found"));

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(true);
        user.setRoles(Collections.singleton(userRole));

        return userRepository.save(user);
    }

    /**
     * Включает или отключает учетную запись пользователя.
     */
    @Transactional
    public void setUserEnabled(Long userId, boolean enabled) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        user.setEnabled(enabled);
        userRepository.save(user);
    }

    /**
     * Возвращеает нового пользователя по username.
     */
    public User getByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    /**
     * Возвращает постраничный список пользователей
     * с возможностью фильтрации по username и флагу "enabled".
     */
    public Page<UserDto> getUsers(String usernameFilter, Boolean enabled, Pageable pageable) {
        Page<User> page;
        if (usernameFilter != null && !usernameFilter.isBlank() && enabled != null) {
            page = userRepository.findByUsernameContainingIgnoreCaseAndEnabled(usernameFilter, enabled, pageable);
        } else if (usernameFilter != null && !usernameFilter.isBlank()) {
            page = userRepository.findByUsernameContainingIgnoreCase(usernameFilter, pageable);
        } else if (enabled != null) {
            page = userRepository.findByEnabled(enabled, pageable);
        } else {
            page = userRepository.findAll(pageable);
        }
        return page.map(this::toDto);
    }

    private UserDto toDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEnabled(user.isEnabled());
        dto.setRoles(user.getRoles().stream()
                .map(Role::getName)
                .collect(java.util.stream.Collectors.toSet()));
        return dto;
    }
}
