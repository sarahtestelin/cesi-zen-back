package com.cesi_zen_back.cesi_zen_back.service;

import com.cesi_zen_back.cesi_zen_back.dto.AppUserDto;
import com.cesi_zen_back.cesi_zen_back.dto.UpdateCurrentUserDto;
import com.cesi_zen_back.cesi_zen_back.dto.UserDataExportDto;
import com.cesi_zen_back.cesi_zen_back.entity.*;
import com.cesi_zen_back.cesi_zen_back.exception.BadRequestException;
import com.cesi_zen_back.cesi_zen_back.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppUserServiceImplTest {

    @Mock private AppUserRepository appUserRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private ResetPasswordRepository resetPasswordRepository;
    @Mock private DiagnosticResultRepository diagnosticResultRepository;
    @Mock private HistoricEtatRepository historicEtatRepository;
    @Mock private AdminAuditLogRepository adminAuditLogRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AdminAuditService adminAuditService;

    @InjectMocks
    private AppUserServiceImpl service;

    private UUID userId;
    private Role userRole;
    private AppUser user;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        userRole = Role.builder()
                .roleId(UUID.randomUUID())
                .roleName("USER")
                .build();

        user = AppUser.builder()
                .idUser(userId)
                .mail("user@test.fr")
                .pseudo("Sarah")
                .hashedPassword("hashed-password")
                .isActive(true)
                .lastConnexion(LocalDateTime.now())
                .role(userRole)
                .build();
    }

    @Test
    void getCurrentUser_shouldReturnCurrentUser() {
        when(appUserRepository.findByMail("user@test.fr")).thenReturn(Optional.of(user));

        AppUserDto result = service.getCurrentUser("user@test.fr");

        assertThat(result.id()).isEqualTo(userId);
        assertThat(result.mail()).isEqualTo("user@test.fr");
        assertThat(result.pseudo()).isEqualTo("Sarah");
        assertThat(result.role()).isEqualTo("USER");
    }

    @Test
    void getCurrentUser_shouldThrow_whenUserDoesNotExist() {
        when(appUserRepository.findByMail("missing@test.fr")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getCurrentUser("missing@test.fr"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Utilisateur introuvable.");
    }

    @Test
    void updateCurrentUser_shouldUpdateMailAndPseudo() {
        UpdateCurrentUserDto dto = new UpdateCurrentUserDto("new@test.fr", "NewSarah");

        when(appUserRepository.findByMail("user@test.fr")).thenReturn(Optional.of(user));
        when(appUserRepository.findByMail("new@test.fr")).thenReturn(Optional.empty());
        when(appUserRepository.existsByPseudo("NewSarah")).thenReturn(false);
        when(appUserRepository.save(user)).thenReturn(user);

        AppUserDto result = service.updateCurrentUser("user@test.fr", dto);

        assertThat(result.mail()).isEqualTo("new@test.fr");
        assertThat(result.pseudo()).isEqualTo("NewSarah");
        verify(appUserRepository).save(user);
    }

    @Test
    void updateCurrentUser_shouldRejectMailAlreadyUsedByAnotherUser() {
        AppUser otherUser = AppUser.builder()
                .idUser(UUID.randomUUID())
                .mail("new@test.fr")
                .pseudo("Other")
                .role(userRole)
                .isActive(true)
                .build();

        UpdateCurrentUserDto dto = new UpdateCurrentUserDto("new@test.fr", "Sarah2");

        when(appUserRepository.findByMail("user@test.fr")).thenReturn(Optional.of(user));
        when(appUserRepository.findByMail("new@test.fr")).thenReturn(Optional.of(otherUser));

        assertThatThrownBy(() -> service.updateCurrentUser("user@test.fr", dto))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Un compte existe déjà avec cet email.");

        verify(appUserRepository, never()).save(any());
    }

    @Test
    void updateCurrentUser_shouldRejectPseudoAlreadyUsed() {
        UpdateCurrentUserDto dto = new UpdateCurrentUserDto("new@test.fr", "ExistingPseudo");

        when(appUserRepository.findByMail("user@test.fr")).thenReturn(Optional.of(user));
        when(appUserRepository.findByMail("new@test.fr")).thenReturn(Optional.empty());
        when(appUserRepository.existsByPseudo("ExistingPseudo")).thenReturn(true);

        assertThatThrownBy(() -> service.updateCurrentUser("user@test.fr", dto))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Ce pseudo est déjà utilisé.");

        verify(appUserRepository, never()).save(any());
    }

    @Test
    void exportCurrentUserData_shouldReturnUserDataDiagnosticsHistoriesAndAuditLogs() {
        DiagnosticResult diagnostic = new DiagnosticResult();
        diagnostic.setId(UUID.randomUUID());
        diagnostic.setFinalScore(180);
        diagnostic.setLevel("MODERE");
        diagnostic.setMessage("Stress modéré");
        diagnostic.setCreatedAt(LocalDateTime.now());

        HistoricEtat history = new HistoricEtat();
        history.setId(UUID.randomUUID());
        history.setOldValue("old");
        history.setNewValue("new");
        history.setComment("UPDATE");
        history.setEntityType("APP_USER");
        history.setEntityId(userId);
        history.setModificationDate(LocalDateTime.now());

        AdminAuditLog auditLog = AdminAuditLog.builder()
                .id(UUID.randomUUID())
                .action("UPDATE_USER")
                .targetType("APP_USER")
                .targetId(userId.toString())
                .details("Modification")
                .createdAt(LocalDateTime.now())
                .build();

        when(appUserRepository.findByMail("user@test.fr")).thenReturn(Optional.of(user));
        when(diagnosticResultRepository.findByAppUserIdUserOrderByCreatedAtDesc(userId)).thenReturn(List.of(diagnostic));
        when(historicEtatRepository.findByAppUserIdUserOrderByModificationDateDesc(userId)).thenReturn(List.of(history));
        when(historicEtatRepository.findByEntityTypeAndEntityIdOrderByModificationDateDesc("APP_USER", userId)).thenReturn(List.of(history));
        when(adminAuditLogRepository.findByTargetIdOrderByCreatedAtDesc(userId.toString())).thenReturn(List.of(auditLog));

        UserDataExportDto result = service.exportCurrentUserData("user@test.fr");

        assertThat(result.id()).isEqualTo(userId);
        assertThat(result.mail()).isEqualTo("user@test.fr");
        assertThat(result.diagnosticResults()).hasSize(1);
        assertThat(result.histories()).hasSize(1);
        assertThat(result.auditLogs()).hasSize(1);
    }

    @Test
    void anonymizeCurrentUser_shouldAnonymizeUserAndDetachSensitiveData() {
        DiagnosticResult diagnostic = new DiagnosticResult();
        diagnostic.setAppUser(user);

        HistoricEtat history = new HistoricEtat();
        history.setId(UUID.randomUUID());
        history.setAppUser(user);
        history.setEntityId(userId);
        history.setOldValue("old");
        history.setNewValue("new");
        history.setComment("old comment");

        when(appUserRepository.findByMail("user@test.fr")).thenReturn(Optional.of(user));
        when(diagnosticResultRepository.findByAppUserIdUser(userId)).thenReturn(List.of(diagnostic));
        when(historicEtatRepository.findByAppUserIdUserOrderByModificationDateDesc(userId)).thenReturn(List.of(history));
        when(historicEtatRepository.findByEntityTypeAndEntityIdOrderByModificationDateDesc("APP_USER", userId)).thenReturn(List.of(history));
        when(passwordEncoder.encode(anyString())).thenReturn("anonymous-password");

        service.anonymizeCurrentUser("user@test.fr");

        assertThat(user.getMail()).isEqualTo("deleted-user-" + userId + "@deleted.local");
        assertThat(user.getPseudo()).isEqualTo("deleted-user-" + userId);
        assertThat(user.isActive()).isFalse();
        assertThat(user.getHashedPassword()).isEqualTo("anonymous-password");

        assertThat(diagnostic.getAppUser()).isNull();
        assertThat(history.getAppUser()).isNull();
        assertThat(history.getEntityId()).isNull();
        assertThat(history.getOldValue()).isEqualTo("[ANONYMIZED_RGPD]");
        assertThat(history.getNewValue()).isEqualTo("[ANONYMIZED_RGPD]");

        verify(refreshTokenRepository).deleteByUser(user);
        verify(resetPasswordRepository).deleteByUser(user);
        verify(diagnosticResultRepository).saveAll(List.of(diagnostic));
        verify(historicEtatRepository).saveAll(List.of(history));
        verify(appUserRepository).save(user);
    }

    @Test
    void disableUser_shouldDisableUserAndLogAdminAction() {
        when(appUserRepository.findById(userId)).thenReturn(Optional.of(user));
        when(appUserRepository.save(user)).thenReturn(user);

        AppUserDto result = service.disableUser(userId, "admin@test.fr");

        assertThat(result.appUserIsActive()).isFalse();

        verify(adminAuditService).log(
                eq("admin@test.fr"),
                eq("DISABLE_USER"),
                eq("APP_USER"),
                eq(userId.toString()),
                anyString()
        );
    }

    @Test
    void enableUser_shouldEnableUserAndLogAdminAction() {
        user.setActive(false);

        when(appUserRepository.findById(userId)).thenReturn(Optional.of(user));
        when(appUserRepository.save(user)).thenReturn(user);

        AppUserDto result = service.enableUser(userId, "admin@test.fr");

        assertThat(result.appUserIsActive()).isTrue();

        verify(adminAuditService).log(
                eq("admin@test.fr"),
                eq("ENABLE_USER"),
                eq("APP_USER"),
                eq(userId.toString()),
                anyString()
        );
    }

    @Test
    void deleteUser_shouldAnonymizeUserAndLogAdminAction() {
        when(appUserRepository.findById(userId)).thenReturn(Optional.of(user));
        when(diagnosticResultRepository.findByAppUserIdUser(userId)).thenReturn(List.of());
        when(historicEtatRepository.findByAppUserIdUserOrderByModificationDateDesc(userId)).thenReturn(List.of());
        when(historicEtatRepository.findByEntityTypeAndEntityIdOrderByModificationDateDesc("APP_USER", userId)).thenReturn(List.of());
        when(passwordEncoder.encode(anyString())).thenReturn("anonymous-password");

        service.deleteUser(userId, "admin@test.fr");

        assertThat(user.isActive()).isFalse();
        assertThat(user.getMail()).contains("@deleted.local");

        verify(adminAuditService).log(
                eq("admin@test.fr"),
                eq("ANONYMIZE_USER"),
                eq("APP_USER"),
                eq(userId.toString()),
                anyString()
        );
    }

    @Test
    void getUserById_shouldThrow_whenUserDoesNotExist() {
        UUID missingId = UUID.randomUUID();

        when(appUserRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getUserById(missingId))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Utilisateur introuvable.");
    }
}