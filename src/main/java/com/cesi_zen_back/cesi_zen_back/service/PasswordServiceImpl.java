package com.cesi_zen_back.cesi_zen_back.service;

import com.cesi_zen_back.cesi_zen_back.dto.ChangePasswordDto;
import com.cesi_zen_back.cesi_zen_back.dto.ForgotPasswordDto;
import com.cesi_zen_back.cesi_zen_back.dto.ResetPasswordDto;
import com.cesi_zen_back.cesi_zen_back.entity.AppUser;
import com.cesi_zen_back.cesi_zen_back.entity.ResetPassword;
import com.cesi_zen_back.cesi_zen_back.exception.BadRequestException;
import com.cesi_zen_back.cesi_zen_back.repository.AppUserRepository;
import com.cesi_zen_back.cesi_zen_back.repository.ResetPasswordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordServiceImpl implements PasswordService {

    private final AppUserRepository appUserRepository;
    private final ResetPasswordRepository resetPasswordRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.frontend.reset-password-url}")
    private String resetPasswordUrl;

    @Override
    @Transactional
    public void requestResetPassword(ForgotPasswordDto dto) {
        appUserRepository.findByMail(dto.mail()).ifPresent(user -> {
            resetPasswordRepository.deleteByUser(user);

            ResetPassword resetPassword = ResetPassword.builder()
                    .tokenDemandReset(UUID.randomUUID().toString())
                    .createdAt(LocalDateTime.now())
                    .expirationDate(LocalDateTime.now().plusMinutes(30))
                    .used(false)
                    .user(user)
                    .build();

            resetPasswordRepository.save(resetPassword);

            String resetLink = resetPasswordUrl + "?token=" + resetPassword.getTokenDemandReset();

            emailService.sendResetPasswordEmail(
                    user.getMail(),
                    user.getPseudo(),
                    resetLink
            );
        });
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordDto dto) {
        ResetPassword resetPassword = resetPasswordRepository.findByTokenDemandReset(dto.token())
                .orElseThrow(() -> new BadRequestException("Token de réinitialisation invalide."));

        if (resetPassword.isUsed()) {
            throw new BadRequestException("Token déjà utilisé.");
        }

        if (resetPassword.getExpirationDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Token expiré.");
        }

        AppUser user = resetPassword.getUser();
        user.setHashedPassword(passwordEncoder.encode(dto.newPassword()));

        resetPassword.setUsed(true);

        appUserRepository.save(user);
        resetPasswordRepository.save(resetPassword);
    }

    @Override
    @Transactional
    public void changePassword(String mail, ChangePasswordDto dto) {
        AppUser user = appUserRepository.findByMail(mail)
                .orElseThrow(() -> new BadRequestException("Utilisateur introuvable."));

        if (!passwordEncoder.matches(dto.currentPassword(), user.getHashedPassword())) {
            throw new BadRequestException("Mot de passe actuel incorrect.");
        }

        user.setHashedPassword(passwordEncoder.encode(dto.newPassword()));
        appUserRepository.save(user);
    }
}