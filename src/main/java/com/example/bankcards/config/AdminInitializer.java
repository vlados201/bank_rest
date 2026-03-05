package com.example.bankcards.config;

import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@RequiredArgsConstructor
public class AdminInitializer {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    @Transactional
    public void createAdminIfNotExists() {
        if (userRepository.findByUsername("admin").isPresent()) {
            return;
        }

        Role adminRole = roleRepository.findAll().stream()
                .filter(r -> "ROLE_ADMIN".equals(r.getName()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("ROLE_ADMIN not found in roles table"));

        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("222333"));
        admin.setEnabled(true);
        admin.setRoles(Collections.singleton(adminRole));

        userRepository.save(admin);
    }
}

