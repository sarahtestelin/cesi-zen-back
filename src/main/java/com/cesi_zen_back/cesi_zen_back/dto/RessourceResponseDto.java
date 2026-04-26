package com.cesi_zen_back.cesi_zen_back.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record RessourceResponseDto(
        UUID id,
        String title,
        String description,
        String category,
        boolean ressourceIsActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Integer version
) {}