package com.cesi_zen_back.cesi_zen_back.controller;

import com.cesi_zen_back.cesi_zen_back.dto.AuthResponseDto;
import com.cesi_zen_back.cesi_zen_back.dto.LoginDto;
import com.cesi_zen_back.cesi_zen_back.dto.RegisterUserDto;
import com.cesi_zen_back.cesi_zen_back.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

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
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        AuthResponseDto authResponse = authService.refresh(refreshToken);
        addRefreshTokenCookie(response, authResponse.refreshToken());
        return ResponseEntity.ok(authResponse.withoutRefreshToken());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        authService.logout(refreshToken);
        deleteRefreshTokenCookie(response);
        return ResponseEntity.noContent().build();
    }

    private void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/api/auth");
        cookie.setMaxAge(7 * 24 * 60 * 60);
        response.addCookie(cookie);
    }

    private void deleteRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("refreshToken", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/api/auth");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}