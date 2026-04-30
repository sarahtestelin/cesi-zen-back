package com.cesi_zen_back.cesi_zen_back.service;

import com.cesi_zen_back.cesi_zen_back.dto.AppUserDto;
import com.cesi_zen_back.cesi_zen_back.dto.DiagnosticResultResponseDto;
import com.cesi_zen_back.cesi_zen_back.dto.UpdateCurrentUserDto;
import com.cesi_zen_back.cesi_zen_back.dto.UserDataExportDto;
import com.cesi_zen_back.cesi_zen_back.entity.AppUser;
import com.cesi_zen_back.cesi_zen_back.entity.DiagnosticResult;
import com.cesi_zen_back.cesi_zen_back.exception.BadRequestException;
import com.cesi_zen_back.cesi_zen_back.mapper.UserMapper;
import com.cesi_zen_back.cesi_zen_back.repository.AppUserRepository;
import com.cesi_zen_back.cesi_zen_back.repository.DiagnosticResultRepository;
import com.cesi_zen_back.cesi_zen_back.repository.RefreshTokenRepository;
import com.cesi_zen_back.cesi_zen_back.repository.ResetPasswordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final PasswordEncoder passwordEncoder;
    private final AdminAuditService adminAuditService;

    @Override
    public AppUserDto getCurrentUser(String mail) {
        AppUser user = findUserByMail(mail);
        return UserMapper.toDto(user);
    }

    @Override
    public UserDataExportDto exportCurrentUserData(String mail) {
        AppUser user = findUserByMail(mail);

        List<DiagnosticResultResponseDto> diagnosticResults =
                diagnosticResultRepository.findByAppUserIdUserOrderByCreatedAtDesc(user.getIdUser())
                        .stream()
                        .map(this::toDiagnosticResultDto)
                        .toList();

        return new UserDataExportDto(
                user.getIdUser(),
                user.getMail(),
                user.getPseudo(),
                user.isActive(),
                user.getLastConnexion(),
                user.getRole().getRoleName(),
                diagnosticResults
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

    private void anonymizeUser(AppUser user) {
        UUID userId = user.getIdUser();
        String anonymizedValue = "deleted-user-" + userId;

        refreshTokenRepository.deleteByUser(user);
        resetPasswordRepository.deleteByUser(user);

        List<DiagnosticResult> diagnosticResults = diagnosticResultRepository.findByAppUserIdUser(userId);
        diagnosticResults.forEach(result -> result.setAppUser(null));
        diagnosticResultRepository.saveAll(diagnosticResults);

        user.setMail(anonymizedValue + "@deleted.local");
        user.setPseudo(anonymizedValue);
        user.setActive(false);
        user.setHashedPassword(passwordEncoder.encode(UUID.randomUUID().toString()));

        appUserRepository.save(user);
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
}