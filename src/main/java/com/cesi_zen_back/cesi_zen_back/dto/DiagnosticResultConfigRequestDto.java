package com.cesi_zen_back.cesi_zen_back.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record DiagnosticResultConfigRequestDto(
        @Min(0) int minScore,
        @Min(0) int maxScore,
        @NotBlank String level,
        @NotBlank String message
) {}