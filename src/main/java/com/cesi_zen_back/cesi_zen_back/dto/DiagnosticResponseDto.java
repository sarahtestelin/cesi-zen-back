package com.cesi_zen_back.cesi_zen_back.dto;

import java.util.UUID;

public record DiagnosticResponseDto(
        UUID resultId,
        int finalScore,
        String level,
        String message
) {}