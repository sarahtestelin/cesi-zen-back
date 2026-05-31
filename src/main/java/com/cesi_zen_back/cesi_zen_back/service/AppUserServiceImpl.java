package com.cesi_zen_back.cesi_zen_back.service;

import com.cesi_zen_back.cesi_zen_back.dto.AdminAuditLogExportDto;
import com.cesi_zen_back.cesi_zen_back.dto.AppUserDto;
import com.cesi_zen_back.cesi_zen_back.dto.DiagnosticResultResponseDto;
import com.cesi_zen_back.cesi_zen_back.dto.HistoricEtatResponseDto;
import com.cesi_zen_back.cesi_zen_back.dto.UpdateCurrentUserDto;
import com.cesi_zen_back.cesi_zen_back.dto.UserDataExportDto;
import com.cesi_zen_back.cesi_zen_back.entity.AdminAuditLog;
import com.cesi_zen_back.cesi_zen_back.entity.AppUser;
import com.cesi_zen_back.cesi_zen_back.entity.DiagnosticResult;
import com.cesi_zen_back.cesi_zen_back.entity.HistoricEtat;
import com.cesi_zen_back.cesi_zen_back.entity.Role;
import com.cesi_zen_back.cesi_zen_back.exception.BadRequestException;
import com.cesi_zen_back.cesi_zen_back.mapper.HistoricEtatMapper;
import com.cesi_zen_back.cesi_zen_back.mapper.UserMapper;
import com.cesi_zen_back.cesi_zen_back.repository.AdminAuditLogRepository;
import com.cesi_zen_back.cesi_zen_back.repository.AppUserRepository;
import com.cesi_zen_back.cesi_zen_back.repository.DiagnosticResultRepository;
import com.cesi_zen_back.cesi_zen_back.repository.HistoricEtatRepository;
import com.cesi_zen_back.cesi_zen_back.repository.RefreshTokenRepository;
import com.cesi_zen_back.cesi_zen_back.repository.ResetPasswordRepository;
import com.cesi_zen_back.cesi_zen_back.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppUserServiceImpl implements AppUserService {

    private static final String TARGET_TYPE_USER = "APP_USER";

    private final AppUserRepository appUserRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ResetPasswordRepository resetPasswordRepository;
    private final DiagnosticResultRepository diagnosticResultRepository;
    private final HistoricEtatRepository historicEtatRepository;
    private final AdminAuditLogRepository adminAuditLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final AdminAuditService adminAuditService;

    @Override
    public AppUserDto getCurrentUser(String mail) {
        AppUser user = findUserByMail(mail);
        return UserMapper.toDto(user);
    }

    @Override
    public UserDataExportDto exportCurrentUserData(String mail) {
        AppUser user = findUserByMail(mail);
        UUID userId = user.getIdUser();

        List<DiagnosticResultResponseDto> diagnosticResults =
                diagnosticResultRepository.findByAppUserIdUserOrderByCreatedAtDesc(userId)
                        .stream()
                        .map(this::toDiagnosticResultDto)
                        .toList();

        List<HistoricEtatResponseDto> histories = getUserHistories(userId)
                .stream()
                .map(HistoricEtatMapper::toDto)
                .toList();

        List<AdminAuditLogExportDto> auditLogs =
                adminAuditLogRepository.findByTargetIdOrderByCreatedAtDesc(userId.toString())
                        .stream()
                        .map(this::toAdminAuditLogExportDto)
                        .toList();

        return new UserDataExportDto(
                user.getIdUser(),
                user.getMail(),
                user.getPseudo(),
                user.isActive(),
                user.getLastConnexion(),
                user.getRole().getRoleName(),
                diagnosticResults,
                histories,
                auditLogs
        );
    }

    @Override
    @Transactional
    public AppUserDto updateCurrentUser(String currentMail, UpdateCurrentUserDto dto) {
        AppUser user = findUserByMail(currentMail);

        appUserRepository.findByMail(dto.mail())
                .filter(existingUser -> !existingUser.getIdUser().equals(user.getIdUser()))
                .ifPresent(existingUser -> {
                    throw new BadRequestException("Un compte existe déjà avec cet email.");
                });

        if (!user.getPseudo().equals(dto.pseudo()) && appUserRepository.existsByPseudo(dto.pseudo())) {
            throw new BadRequestException("Ce pseudo est déjà utilisé.");
        }

        user.setMail(dto.mail());
        user.setPseudo(dto.pseudo());

        return UserMapper.toDto(appUserRepository.save(user));
    }

    @Override
    @Transactional
    public void anonymizeCurrentUser(String currentMail) {
        AppUser user = findUserByMail(currentMail);
        anonymizeUser(user);
    }

    @Override
    public List<AppUserDto> getAllUsers() {
        return appUserRepository.findAll()
                .stream()
                .map(UserMapper::toDto)
                .toList();
    }

    @Override
    public AppUserDto getUserById(UUID id) {
        AppUser user = findUserById(id);
        return UserMapper.toDto(user);
    }

    @Override
    @Transactional
    public AppUserDto updateUser(UUID id, AppUserDto appUserDto, String adminMail) {
        AppUser user = findUserById(id);

        user.setMail(appUserDto.mail());
        user.setPseudo(appUserDto.pseudo());
        user.setActive(appUserDto.appUserIsActive());

        AppUser savedUser = appUserRepository.save(user);

        adminAuditService.log(
                adminMail,
                "UPDATE_USER",
                TARGET_TYPE_USER,
                id.toString(),
                "Modification du compte utilisateur par un administrateur."
        );

        return UserMapper.toDto(savedUser);
    }

    @Override
    @Transactional
    public void deleteUser(UUID id, String adminMail) {
        AppUser user = findUserById(id);
        anonymizeUser(user);

        adminAuditService.log(
                adminMail,
                "ANONYMIZE_USER",
                TARGET_TYPE_USER,
                id.toString(),
                "Anonymisation RGPD du compte utilisateur par un administrateur."
        );
    }

    @Override
    @Transactional
    public AppUserDto disableUser(UUID id, String adminMail) {
        AppUser user = findUserById(id);
        user.setActive(false);

        AppUser savedUser = appUserRepository.save(user);

        adminAuditService.log(
                adminMail,
                "DISABLE_USER",
                TARGET_TYPE_USER,
                id.toString(),
                "Désactivation du compte utilisateur par un administrateur."
        );

        return UserMapper.toDto(savedUser);
    }

    @Override
    @Transactional
    public AppUserDto enableUser(UUID id, String adminMail) {
        AppUser user = findUserById(id);
        user.setActive(true);

        AppUser savedUser = appUserRepository.save(user);

        adminAuditService.log(
                adminMail,
                "ENABLE_USER",
                TARGET_TYPE_USER,
                id.toString(),
                "Réactivation du compte utilisateur par un administrateur."
        );

        return UserMapper.toDto(savedUser);
    }

    @Override
    @Transactional
    public AppUserDto promoteUser(UUID id, String adminMail) {
        return changeUserRole(id, adminMail, "ADMIN", "PROMOTE_USER", "Promotion au rôle ADMIN par un administrateur.");
    }

    @Override
    @Transactional
    public AppUserDto demoteUser(UUID id, String adminMail) {
        AppUser admin = findUserByMail(adminMail);
        if (admin.getIdUser().equals(id)) {
            throw new BadRequestException("Vous ne pouvez pas retirer votre propre rôle administrateur.");
        }
        return changeUserRole(id, adminMail, "USER", "DEMOTE_USER", "Rétrogradation au rôle USER par un administrateur.");
    }

    private AppUserDto changeUserRole(UUID id, String adminMail, String roleName, String action, String details) {
        AppUser user = findUserById(id);

        Role role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new BadRequestException("Rôle " + roleName + " introuvable."));

        user.setRole(role);
        AppUser savedUser = appUserRepository.save(user);

        adminAuditService.log(adminMail, action, TARGET_TYPE_USER, id.toString(), details);

        return UserMapper.toDto(savedUser);
    }

    private void anonymizeUser(AppUser user) {
        UUID userId = user.getIdUser();
        String anonymizedValue = "deleted-user-" + userId;

        refreshTokenRepository.deleteByUser(user);
        resetPasswordRepository.deleteByUser(user);

        List<DiagnosticResult> diagnosticResults = diagnosticResultRepository.findByAppUserIdUser(userId);
        diagnosticResults.forEach(result -> result.setAppUser(null));
        diagnosticResultRepository.saveAll(diagnosticResults);

        List<HistoricEtat> histories = getUserHistories(userId);
        histories.forEach(history -> {
            history.setAppUser(null);
            history.setEntityId(null);
            history.setOldValue("[ANONYMIZED_RGPD]");
            history.setNewValue("[ANONYMIZED_RGPD]");
            history.setComment("Historique anonymisé suite à une demande RGPD.");
        });
        historicEtatRepository.saveAll(histories);

        user.setMail(anonymizedValue + "@deleted.local");
        user.setPseudo(anonymizedValue);
        user.setActive(false);
        user.setHashedPassword(passwordEncoder.encode(UUID.randomUUID().toString()));

        appUserRepository.save(user);
    }

    private List<HistoricEtat> getUserHistories(UUID userId) {
        LinkedHashMap<UUID, HistoricEtat> histories = new LinkedHashMap<>();

        historicEtatRepository.findByAppUserIdUserOrderByModificationDateDesc(userId)
                .forEach(history -> histories.put(history.getId(), history));

        historicEtatRepository.findByEntityTypeAndEntityIdOrderByModificationDateDesc(TARGET_TYPE_USER, userId)
                .forEach(history -> histories.put(history.getId(), history));

        return histories.values().stream().toList();
    }

    private AppUser findUserByMail(String mail) {
        return appUserRepository.findByMail(mail)
                .orElseThrow(() -> new BadRequestException("Utilisateur introuvable."));
    }

    private AppUser findUserById(UUID id) {
        return appUserRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Utilisateur introuvable."));
    }

    private DiagnosticResultResponseDto toDiagnosticResultDto(DiagnosticResult result) {
        return new DiagnosticResultResponseDto(
                result.getId(),
                result.getFinalScore(),
                result.getLevel(),
                result.getMessage(),
                result.getCreatedAt()
        );
    }

    private AdminAuditLogExportDto toAdminAuditLogExportDto(AdminAuditLog log) {
        return new AdminAuditLogExportDto(
                log.getId(),
                log.getAction(),
                log.getTargetType(),
                log.getTargetId(),
                log.getDetails(),
                log.getCreatedAt()
        );
    }
}