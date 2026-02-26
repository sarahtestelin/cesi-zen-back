package com.cesi_zen_back.cesi_zen_back.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record AppUserDto(
        UUID id,
        String mail,
        String pseudo,
        boolean appUserIsActive,
        LocalDateTime lastConnectionAt
) {}