package com.cesi_zen_back.cesi_zen_back.dto;

public record AuthResponseDto(
        String accessToken,
        String refreshToken,
        AppUserDto user
) {
    public AuthResponseDto withoutRefreshToken() {
        return new AuthResponseDto(accessToken, null, user);
    }
}