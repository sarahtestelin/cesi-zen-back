package com.cesi_zen_back.cesi_zen_back.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record DiagnosticQuestionResponseDto(
        UUID id,
        String question,
        int score,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}