package com.cesi_zen_back.cesi_zen_back.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateCurrentUserDto(
        @NotBlank(message = "L'email est obligatoire.")
        @Email(message = "L'email doit être valide.")
        @Size(max = 150, message = "L'email ne doit pas dépasser 150 caractères.")
        String mail,

        @NotBlank(message = "Le pseudo est obligatoire.")
        @Size(max = 150, message = "Le pseudo ne doit pas dépasser 150 caractères.")
        String pseudo
) {
}