package com.cesi_zen_back.cesi_zen_back.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DiagnosticQuestionRequestDto(
        @NotBlank @Size(max = 255) String question,
        @Min(0) int score
) {}