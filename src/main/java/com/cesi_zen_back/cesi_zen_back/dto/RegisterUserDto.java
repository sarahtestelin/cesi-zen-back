package com.cesi_zen_back.cesi_zen_back.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterUserDto(
        @Email @NotBlank String mail,
        @NotBlank @Size(min = 3, max = 150) String pseudo,
        @NotBlank @Size(min = 12, message = "Le mot de passe doit contenir au moins 12 caractères.") String password,
        String deviceInfo
) {}