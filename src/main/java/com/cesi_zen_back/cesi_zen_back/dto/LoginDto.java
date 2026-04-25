package com.cesi_zen_back.cesi_zen_back.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginDto(
        @Email @NotBlank String mail,
        @NotBlank String password,
        String deviceInfo
) {}