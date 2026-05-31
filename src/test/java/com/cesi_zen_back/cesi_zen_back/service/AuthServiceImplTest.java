package com.cesi_zen_back.cesi_zen_back.service;

import com.cesi_zen_back.cesi_zen_back.dto.AuthResponseDto;
import com.cesi_zen_back.cesi_zen_back.dto.LoginDto;
import com.cesi_zen_back.cesi_zen_back.dto.RegisterUserDto;
import com.cesi_zen_back.cesi_zen_back.entity.AppUser;
import com.cesi_zen_back.cesi_zen_back.entity.RefreshToken;
import com.cesi_zen_back.cesi_zen_back.entity.Role;
import com.cesi_zen_back.cesi_zen_back.exception.BadRequestException;
import com.cesi_zen_back.cesi_zen_back.repository.AppUserRepository;
import com.cesi_zen_back.cesi_zen_back.repository.RefreshTokenRepository;
import com.cesi_zen_back.cesi_zen_back.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private AppUserRepository appUserRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;

    @InjectMocks private AuthServiceImpl authService;

    private Role userRole;
    private AppUser activeUser;

    @BeforeEach
    void setUp() {
        userRole = Role.builder()
                .roleId(UUID.randomUUID())
                .roleName("USER")
                .build();

        activeUser = AppUser.builder()
                .idUser(UUID.randomUUID())
                .mail("user@test.fr")
                .pseudo("Sarah")
                .hashedPassword("encoded-password")
                .isActive(true)
                .role(userRole)
                .lastConnexion(LocalDateTime.now())
                .build();
    }

    @Test
    void register_shouldCreateUserAndRefreshToken_whenDataIsValid() {
        RegisterUserDto dto = new RegisterUserDto("user@test.fr", "Sarah", "Password123!", "Terminal");

        when(appUserRepository.existsByMail(dto.mail())).thenReturn(false);
        when(appUserRepository.existsByPseudo(dto.pseudo())).thenReturn(false);
        when(roleRepository.findByRoleName("USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(dto.password())).thenReturn("encoded-password");
        when(appUserRepository.save(any(AppUser.class))).thenReturn(activeUser);
        when(jwtService.generateAccessToken(activeUser)).thenReturn("access-token");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthResponseDto response = authService.register(dto);

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isNotBlank();
        assertThat(response.user().mail()).isEqualTo("user@test.fr");
        assertThat(response.user().pseudo()).isEqualTo("Sarah");

        verify(appUserRepository).save(any(AppUser.class));
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void register_shouldRejectDuplicateMail() {
        RegisterUserDto dto = new RegisterUserDto("user@test.fr", "Sarah", "Password123!", "Terminal");

        when(appUserRepository.existsByMail(dto.mail())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(dto))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Un compte existe déjà avec cet email.");

        verify(appUserRepository, never()).save(any());
    }

    @Test
    void login_shouldReturnTokens_whenCredentialsAreValid() {
        LoginDto dto = new LoginDto("user@test.fr", "Password123!", "Terminal");

        when(appUserRepository.findByMail(dto.mail())).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches(dto.password(), activeUser.getHashedPassword())).thenReturn(true);
        when(appUserRepository.save(activeUser)).thenReturn(activeUser);
        when(jwtService.generateAccessToken(activeUser)).thenReturn("access-token");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthResponseDto response = authService.login(dto);

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isNotBlank();

        verify(appUserRepository).save(activeUser);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void login_shouldRejectInactiveAccount() {
        activeUser.setActive(false);
        LoginDto dto = new LoginDto("user@test.fr", "Password123!", "Terminal");

        when(appUserRepository.findByMail(dto.mail())).thenReturn(Optional.of(activeUser));

        assertThatThrownBy(() -> authService.login(dto))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Ce compte est désactivé.");

        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void login_shouldRejectInvalidPassword() {
        LoginDto dto = new LoginDto("user@test.fr", "WrongPassword123!", "Terminal");

        when(appUserRepository.findByMail(dto.mail())).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches(dto.password(), activeUser.getHashedPassword())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(dto))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Identifiants incorrects.");

        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void refresh_shouldRotateRefreshToken_whenTokenIsValid() {
        RefreshToken oldToken = RefreshToken.builder()
                .token("old-refresh-token")
                .creationDate(LocalDateTime.now().minusDays(1))
                .expirationDate(LocalDateTime.now().plusDays(6))
                .isRevoked(false)
                .deviceInfo("Terminal")
                .user(activeUser)
                .build();

        when(refreshTokenRepository.findByToken("old-refresh-token")).thenReturn(Optional.of(oldToken));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateAccessToken(activeUser)).thenReturn("new-access-token");

        AuthResponseDto response = authService.refresh("old-refresh-token");

        assertThat(response.accessToken()).isEqualTo("new-access-token");
        assertThat(response.refreshToken()).isNotBlank();
        assertThat(response.refreshToken()).isNotEqualTo("old-refresh-token");
        assertThat(oldToken.isRevoked()).isTrue();

        verify(refreshTokenRepository, times(2)).save(any(RefreshToken.class));
    }

}