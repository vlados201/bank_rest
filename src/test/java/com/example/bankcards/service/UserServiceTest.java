package com.example.bankcards.service;

import com.example.bankcards.dto.RegisterUserRequestDto;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        roleRepository = mock(RoleRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        userService = new UserService(userRepository, roleRepository, passwordEncoder);
    }

    @Test
    void registerUser_ShouldCreateUserWithUserRole() {
        RegisterUserRequestDto request = new RegisterUserRequestDto();
        request.setUsername("user1");
        request.setPassword("pass");

        when(userRepository.findByUsername("user1")).thenReturn(Optional.empty());
        Role roleUser = new Role();
        roleUser.setId(1L);
        roleUser.setName("ROLE_USER");
        when(userRepository.findByUsername("user1")).thenReturn(Optional.empty());
        when(roleRepository.findAll()).thenReturn(List.of(roleUser));
        when(passwordEncoder.encode("pass")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User created = userService.registerUser(request);

        assertThat(created.getUsername()).isEqualTo("user1");
        assertThat(created.getPassword()).isEqualTo("encoded");
        assertThat(created.isEnabled()).isTrue();
        assertThat(created.getRoles()).containsExactly(roleUser);
    }

    @Test
    void getUsers_ShouldReturnMappedDtos() {
        User user = new User();
        user.setId(1L);
        user.setUsername("user1");
        user.setEnabled(true);
        Role role = new Role();
        role.setName("ROLE_USER");
        user.setRoles(Collections.singleton(role));

        when(userRepository.findAll(PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(user)));

        Page<UserDto> page = userService.getUsers(null, null, PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(1);
        UserDto dto = page.getContent().get(0);
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getUsername()).isEqualTo("user1");
        assertThat(dto.isEnabled()).isTrue();
        assertThat(dto.getRoles()).containsExactly("ROLE_USER");
    }

    @Test
    void setUserEnabled_ShouldUpdateFlag() {
        User user = new User();
        user.setId(1L);
        user.setEnabled(true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.setUserEnabled(1L, false);

        assertThat(user.isEnabled()).isFalse();
        verify(userRepository).save(user);
    }

    @Test
    void setUserEnabled_WhenUserNotFound_ShouldThrow() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.setUserEnabled(1L, true))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    void getByUsername_ShouldReturnUser() {

        User user = new User();
        user.setId(1L);
        user.setUsername("user1");

        when(userRepository.findByUsername("user1"))
                .thenReturn(Optional.of(user));

        User result = userService.getByUsername("user1");
        assertThat(result).isEqualTo(user);
    }

    @Test
    void getByUsername_WhenNotFound_ShouldThrow() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getByUsername("unknown"))
                .isInstanceOf(EntityNotFoundException.class);
    }
}

