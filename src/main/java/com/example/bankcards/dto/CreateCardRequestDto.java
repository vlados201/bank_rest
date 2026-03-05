package com.example.bankcards.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateCardRequestDto {

    @NotNull
    private Long ownerId;
}
