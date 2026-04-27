package com.cesi_zen_back.cesi_zen_back.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record HistoricEtatResponseDto(
        UUID id,
        String oldValue,
        String newValue,
        String comment,
        String entityType,
        UUID entityId,
        LocalDateTime modificationDate
) {
}