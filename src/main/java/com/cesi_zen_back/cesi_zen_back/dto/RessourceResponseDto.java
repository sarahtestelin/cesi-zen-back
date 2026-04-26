package com.cesi_zen_back.cesi_zen_back.dto;

import com.cesi_zen_back.cesi_zen_back.enums.RessourceStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record RessourceResponseDto(
        UUID id,
        String title,
        String description,
        String category,
        RessourceStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Integer version
) {}