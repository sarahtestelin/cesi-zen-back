package com.cesi_zen_back.cesi_zen_back.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordDto(
        @NotBlank String currentPassword,
        @NotBlank @Size(min = 12) String newPassword
) {}