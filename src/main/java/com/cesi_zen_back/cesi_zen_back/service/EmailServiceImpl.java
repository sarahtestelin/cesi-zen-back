package com.cesi_zen_back.cesi_zen_back.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;

    @Override
    public void sendResetPasswordEmail(String to, String pseudo, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom("no-reply@cesizen.local");
        message.setTo(to);
        message.setSubject("Réinitialisation de votre mot de passe CESIZen");
        message.setText("""
                Bonjour %s,

                Vous avez demandé la réinitialisation de votre mot de passe.

                Cliquez sur le lien suivant pour choisir un nouveau mot de passe :
                %s

                Ce lien expire dans 30 minutes.

                Si vous n'êtes pas à l'origine de cette demande, vous pouvez ignorer cet email.

                L'équipe CESIZen
                """.formatted(pseudo, resetLink));

        javaMailSender.send(message);
    }
}