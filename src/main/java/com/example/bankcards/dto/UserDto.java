package com.example.bankcards.dto;


import lombok.Data;

import java.util.Set;

@Data
public class UserDto {

    private Long id;
    private String username;
    private boolean enabled;
    private Set<String> roles;
}
