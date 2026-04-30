package com.cesi_zen_back.cesi_zen_back.service;

import com.cesi_zen_back.cesi_zen_back.dto.AppUserDto;
import com.cesi_zen_back.cesi_zen_back.dto.UpdateCurrentUserDto;
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

    private final AppUserRepository appUserRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ResetPasswordRepository resetPasswordRepository;
    private final DiagnosticResultRepository diagnosticResultRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AppUserDto getCurrentUser(String mail) {
        AppUser user = appUserRepository.findByMail(mail)
                .orElseThrow(() -> new BadRequestException("Utilisateur introuvable."));

        return UserMapper.toDto(user);
    }

    @Override
    @Transactional
    public AppUserDto updateCurrentUser(String currentMail, UpdateCurrentUserDto dto) {
        AppUser user = appUserRepository.findByMail(currentMail)
                .orElseThrow(() -> new BadRequestException("Utilisateur introuvable."));

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
        AppUser user = appUserRepository.findByMail(currentMail)
                .orElseThrow(() -> new BadRequestException("Utilisateur introuvable."));

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

    @Override
    public List<AppUserDto> getAllUsers() {
        return appUserRepository.findAll()
                .stream()
                .map(UserMapper::toDto)
                .toList();
    }

    @Override
    public AppUserDto getUserById(UUID id) {
        AppUser user = appUserRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Utilisateur introuvable."));

        return UserMapper.toDto(user);
    }

    @Override
    @Transactional
    public AppUserDto updateUser(UUID id, AppUserDto appUserDto) {
        AppUser user = appUserRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Utilisateur introuvable."));

        user.setMail(appUserDto.mail());
        user.setPseudo(appUserDto.pseudo());
        user.setActive(appUserDto.appUserIsActive());

        return UserMapper.toDto(appUserRepository.save(user));
    }

    @Override
    @Transactional
    public void deleteUser(UUID id) {
        if (!appUserRepository.existsById(id)) {
            throw new BadRequestException("Utilisateur introuvable.");
        }

        appUserRepository.deleteById(id);
    }

    @Override
    @Transactional
    public AppUserDto disableUser(UUID id) {
        AppUser user = appUserRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Utilisateur introuvable."));

        user.setActive(false);

        return UserMapper.toDto(appUserRepository.save(user));
    }

    @Override
    @Transactional
    public AppUserDto enableUser(UUID id) {
        AppUser user = appUserRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Utilisateur introuvable."));

        user.setActive(true);

        return UserMapper.toDto(appUserRepository.save(user));
    }
}