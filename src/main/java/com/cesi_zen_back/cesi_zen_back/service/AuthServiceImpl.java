package com.cesi_zen_back.cesi_zen_back.service;

import com.cesi_zen_back.cesi_zen_back.dto.AuthResponseDto;
import com.cesi_zen_back.cesi_zen_back.dto.LoginDto;
import com.cesi_zen_back.cesi_zen_back.dto.RegisterUserDto;
import com.cesi_zen_back.cesi_zen_back.entity.AppUser;
import com.cesi_zen_back.cesi_zen_back.entity.RefreshToken;
import com.cesi_zen_back.cesi_zen_back.entity.Role;
import com.cesi_zen_back.cesi_zen_back.exception.BadRequestException;
import com.cesi_zen_back.cesi_zen_back.mapper.UserMapper;
import com.cesi_zen_back.cesi_zen_back.repository.AppUserRepository;
import com.cesi_zen_back.cesi_zen_back.repository.RefreshTokenRepository;
import com.cesi_zen_back.cesi_zen_back.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AppUserRepository appUserRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    @Transactional
    public AuthResponseDto register(RegisterUserDto dto) {
        if (appUserRepository.existsByMail(dto.mail())) {
            throw new BadRequestException("Un compte existe déjà avec cet email.");
        }

        if (appUserRepository.existsByPseudo(dto.pseudo())) {
            throw new BadRequestException("Ce pseudo est déjà utilisé.");
        }

        Role userRole = roleRepository.findByRoleName("USER")
                .orElseThrow(() -> new BadRequestException("Rôle USER introuvable."));

        AppUser user = AppUser.builder()
                .mail(dto.mail())
                .pseudo(dto.pseudo())
                .hashedPassword(passwordEncoder.encode(dto.password()))
                .role(userRole)
                .isActive(true)
                .lastConnexion(LocalDateTime.now())
                .build();

        AppUser savedUser = appUserRepository.save(user);

        String accessToken = jwtService.generateAccessToken(savedUser);
        String refreshToken = createRefreshToken(savedUser, dto.deviceInfo());

        return new AuthResponseDto(
                accessToken,
                refreshToken,
                UserMapper.toDto(savedUser)
        );
    }

    @Override
    @Transactional
    public AuthResponseDto login(LoginDto dto) {
        AppUser user = appUserRepository.findByMail(dto.mail())
                .orElseThrow(() -> new BadRequestException("Identifiants incorrects."));

        if (!user.isActive()) {
            throw new BadRequestException("Ce compte est désactivé.");
        }

        if (!passwordEncoder.matches(dto.password(), user.getHashedPassword())) {
            throw new BadRequestException("Identifiants incorrects.");
        }

        user.setLastConnexion(LocalDateTime.now());
        appUserRepository.save(user);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = createRefreshToken(user, dto.deviceInfo());

        return new AuthResponseDto(
                accessToken,
                refreshToken,
                UserMapper.toDto(user)
        );
    }

    @Override
    @Transactional
    public AuthResponseDto refresh(String refreshTokenValue) {
        if (refreshTokenValue == null || refreshTokenValue.isBlank()) {
            throw new BadRequestException("Refresh token manquant.");
        }

        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new BadRequestException("Refresh token invalide."));

        if (refreshToken.isRevoked()) {
            throw new BadRequestException("Refresh token révoqué.");
        }

        if (refreshToken.getExpirationDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Refresh token expiré.");
        }

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        AppUser user = refreshToken.getUser();

        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = createRefreshToken(user, refreshToken.getDeviceInfo());

        return new AuthResponseDto(
                newAccessToken,
                newRefreshToken,
                UserMapper.toDto(user)
        );
    }

    @Override
    @Transactional
    public void logout(String refreshTokenValue) {
        if (refreshTokenValue == null || refreshTokenValue.isBlank()) {
            return;
        }

        refreshTokenRepository.findByToken(refreshTokenValue)
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                });
    }

    private String createRefreshToken(AppUser user, String deviceInfo) {
        String tokenValue = UUID.randomUUID().toString();

        RefreshToken refreshToken = RefreshToken.builder()
                .token(tokenValue)
                .creationDate(LocalDateTime.now())
                .expirationDate(LocalDateTime.now().plusDays(7))
                .isRevoked(false)
                .deviceInfo(deviceInfo)
                .user(user)
                .build();

        refreshTokenRepository.save(refreshToken);

        return tokenValue;
    }
}