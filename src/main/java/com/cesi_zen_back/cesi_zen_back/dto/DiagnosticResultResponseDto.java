package com.cesi_zen_back.cesi_zen_back.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record DiagnosticResultResponseDto(
        UUID id,
        int finalScore,
        String level,
        String message,
        LocalDateTime createdAt
) {}