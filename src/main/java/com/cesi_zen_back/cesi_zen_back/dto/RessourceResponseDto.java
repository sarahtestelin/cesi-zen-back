package com.cesi_zen_back.cesi_zen_back.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record RessourceResponseDto(
        UUID id,
        boolean ressourceIsActive,
        boolean ressourceIsUsed,
        String title,
        String description,
        String status,
        String category,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Integer version
) {
}