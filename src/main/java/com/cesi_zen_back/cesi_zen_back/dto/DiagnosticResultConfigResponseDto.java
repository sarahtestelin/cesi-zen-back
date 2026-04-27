package com.cesi_zen_back.cesi_zen_back.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record DiagnosticResultConfigResponseDto(
        UUID id,
        int minScore,
        int maxScore,
        String level,
        String message,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}