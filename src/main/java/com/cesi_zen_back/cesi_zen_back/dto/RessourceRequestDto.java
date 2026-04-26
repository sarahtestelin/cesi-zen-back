package com.cesi_zen_back.cesi_zen_back.dto;

import com.cesi_zen_back.cesi_zen_back.enums.RessourceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RessourceRequestDto(
        @NotBlank @Size(max = 150) String title,
        @NotBlank String description,
        @NotBlank @Size(max = 100) String category,
        @NotNull RessourceStatus status
) {}