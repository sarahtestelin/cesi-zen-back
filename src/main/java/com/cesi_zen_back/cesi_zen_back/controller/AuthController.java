package com.cesi_zen_back.cesi_zen_back.controller;

import com.cesi_zen_back.cesi_zen_back.dto.AuthResponseDto;
import com.cesi_zen_back.cesi_zen_back.dto.LoginDto;
import com.cesi_zen_back.cesi_zen_back.dto.RegisterUserDto;
import com.cesi_zen_back.cesi_zen_back.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    private static final Duration REFRESH_TOKEN_COOKIE_DURATION = Duration.ofDays(7);

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(
            @Valid @RequestBody RegisterUserDto dto,
            HttpServletResponse response
    ) {
        AuthResponseDto authResponse = authService.register(dto);
        addRefreshTokenCookie(response, authResponse.refreshToken());
        return ResponseEntity.ok(authResponse.withoutRefreshToken());
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(
            @Valid @RequestBody LoginDto dto,
            HttpServletResponse response
    ) {
        AuthResponseDto authResponse = authService.login(dto);
        addRefreshTokenCookie(response, authResponse.refreshToken());
        return ResponseEntity.ok(authResponse.withoutRefreshToken());
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDto> refresh(
            @CookieValue(value = REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken,
            HttpServletResponse response
    ) {
        AuthResponseDto authResponse = authService.refresh(refreshToken);
        addRefreshTokenCookie(response, authResponse.refreshToken());
        return ResponseEntity.ok(authResponse.withoutRefreshToken());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(value = REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken,
            HttpServletResponse response
    ) {
        authService.logout(refreshToken);
        deleteRefreshTokenCookie(response);
        return ResponseEntity.noContent().build();
    }

    private void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/api/auth")
                .maxAge(REFRESH_TOKEN_COOKIE_DURATION)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void deleteRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/api/auth")
                .maxAge(0)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}