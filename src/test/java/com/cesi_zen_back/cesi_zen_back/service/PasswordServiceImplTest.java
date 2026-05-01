package com.cesi_zen_back.cesi_zen_back.service;

import com.cesi_zen_back.cesi_zen_back.dto.ChangePasswordDto;
import com.cesi_zen_back.cesi_zen_back.dto.ForgotPasswordDto;
import com.cesi_zen_back.cesi_zen_back.dto.ResetPasswordDto;
import com.cesi_zen_back.cesi_zen_back.entity.AppUser;
import com.cesi_zen_back.cesi_zen_back.entity.ResetPassword;
import com.cesi_zen_back.cesi_zen_back.entity.Role;
import com.cesi_zen_back.cesi_zen_back.exception.BadRequestException;
import com.cesi_zen_back.cesi_zen_back.repository.AppUserRepository;
import com.cesi_zen_back.cesi_zen_back.repository.ResetPasswordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordServiceImplTest {

    @Mock private AppUserRepository appUserRepository;
    @Mock private ResetPasswordRepository resetPasswordRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private EmailService emailService;

    @InjectMocks private PasswordServiceImpl passwordService;

    private AppUser user;

    @BeforeEach
    void setUp() {
        Role role = Role.builder()
                .roleId(UUID.randomUUID())
                .roleName("USER")
                .build();

        user = AppUser.builder()
                .idUser(UUID.randomUUID())
                .mail("user@test.fr")
                .pseudo("Sarah")
                .hashedPassword("old-hashed-password")
                .isActive(true)
                .role(role)
                .lastConnexion(LocalDateTime.now())
                .build();

        ReflectionTestUtils.setField(
                passwordService,
                "resetPasswordUrl",
                "https://localhost:8443/reset-password"
        );
    }

    @Test
    void requestResetPassword_shouldCreateTokenAndSendMail_whenUserExists() {
        when(appUserRepository.findByMail("user@test.fr")).thenReturn(Optional.of(user));
        when(resetPasswordRepository.save(any(ResetPassword.class))).thenAnswer(invocation -> invocation.getArgument(0));

        passwordService.requestResetPassword(new ForgotPasswordDto("user@test.fr"));

        verify(resetPasswordRepository).deleteByUser(user);
        verify(resetPasswordRepository).save(any(ResetPassword.class));
        verify(emailService).sendResetPasswordEmail(
                eq("user@test.fr"),
                eq("Sarah"),
                contains("https://localhost:8443/reset-password?token=")
        );
    }

    @Test
    void requestResetPassword_shouldNotRevealUnknownMail() {
        when(appUserRepository.findByMail("unknown@test.fr")).thenReturn(Optional.empty());

        passwordService.requestResetPassword(new ForgotPasswordDto("unknown@test.fr"));

        verify(resetPasswordRepository, never()).save(any());
        verify(emailService, never()).sendResetPasswordEmail(any(), any(), any());
    }

    @Test
    void resetPassword_shouldUpdatePasswordAndMarkTokenAsUsed_whenTokenIsValid() {
        ResetPassword resetPassword = ResetPassword.builder()
                .tokenDemandReset("valid-token")
                .createdAt(LocalDateTime.now().minusMinutes(5))
                .expirationDate(LocalDateTime.now().plusMinutes(25))
                .used(false)
                .user(user)
                .build();

        when(resetPasswordRepository.findByTokenDemandReset("valid-token"))
                .thenReturn(Optional.of(resetPassword));

        when(passwordEncoder.encode("NewPassword123!"))
                .thenReturn("new-hashed-password");

        passwordService.resetPassword(new ResetPasswordDto("valid-token", "NewPassword123!"));

        assertThat(user.getHashedPassword()).isEqualTo("new-hashed-password");
        assertThat(resetPassword.isUsed()).isTrue();

        verify(appUserRepository).save(user);
        verify(resetPasswordRepository).save(resetPassword);
    }

    @Test
    void resetPassword_shouldRejectExpiredToken() {
        ResetPassword resetPassword = ResetPassword.builder()
                .tokenDemandReset("expired-token")
                .createdAt(LocalDateTime.now().minusHours(1))
                .expirationDate(LocalDateTime.now().minusMinutes(1))
                .used(false)
                .user(user)
                .build();

        when(resetPasswordRepository.findByTokenDemandReset("expired-token"))
                .thenReturn(Optional.of(resetPassword));

        assertThatThrownBy(() ->
                passwordService.resetPassword(new ResetPasswordDto("expired-token", "NewPassword123!"))
        )
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Token expiré.");

        verify(appUserRepository, never()).save(any());
    }

    @Test
    void changePassword_shouldUpdatePassword_whenCurrentPasswordIsCorrect() {
        when(appUserRepository.findByMail("user@test.fr")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("OldPassword123!", "old-hashed-password")).thenReturn(true);
        when(passwordEncoder.encode("NewPassword123!")).thenReturn("new-hashed-password");

        passwordService.changePassword(
                "user@test.fr",
                new ChangePasswordDto("OldPassword123!", "NewPassword123!")
        );

        assertThat(user.getHashedPassword()).isEqualTo("new-hashed-password");
        verify(appUserRepository).save(user);
    }

    @Test
    void changePassword_shouldRejectWrongCurrentPassword() {
        when(appUserRepository.findByMail("user@test.fr")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPassword123!", "old-hashed-password")).thenReturn(false);

        assertThatThrownBy(() ->
                passwordService.changePassword(
                        "user@test.fr",
                        new ChangePasswordDto("WrongPassword123!", "NewPassword123!")
                )
        )
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Mot de passe actuel incorrect.");

        verify(appUserRepository, never()).save(any());
    }
}