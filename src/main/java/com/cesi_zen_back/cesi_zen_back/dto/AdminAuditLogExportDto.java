package com.cesi_zen_back.cesi_zen_back.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record AdminAuditLogExportDto(
        UUID id,
        String action,
        String targetType,
        String targetId,
        String details,
        LocalDateTime createdAt
) {
}