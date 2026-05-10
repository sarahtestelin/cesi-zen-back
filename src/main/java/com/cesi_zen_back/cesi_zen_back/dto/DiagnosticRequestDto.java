package com.cesi_zen_back.cesi_zen_back.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

public record DiagnosticRequestDto(
        @NotEmpty List<UUID> questionIds
) {}