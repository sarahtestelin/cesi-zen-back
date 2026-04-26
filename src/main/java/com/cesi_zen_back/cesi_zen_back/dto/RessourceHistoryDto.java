package com.cesi_zen_back.cesi_zen_back.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record RessourceHistoryDto(
        UUID id,
        LocalDateTime modificationDate,
        String oldValue,
        String newValue,
        String comment,
        UUID appUserId
) {}