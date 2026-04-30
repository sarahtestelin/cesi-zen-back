package com.cesi_zen_back.cesi_zen_back.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record UserDataExportDto(
        UUID id,
        String mail,
        String pseudo,
        boolean active,
        LocalDateTime lastConnectionAt,
        String role,
        List<DiagnosticResultResponseDto> diagnosticResults
) {
}