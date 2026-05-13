package com.cesi_zen_back.cesi_zen_back.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;

    @Override
    public void sendResetPasswordEmail(String to, String pseudo, String resetLink) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");

            helper.setFrom("no-reply@cesizen.local");
            helper.setTo(to);
            helper.setSubject("Réinitialisation de votre mot de passe CESIZen");
            helper.setText("""
                    <div style="font-family: sans-serif; max-width: 480px; margin: 0 auto;">
                        <h2 style="color: #6E4D36;">Bonjour %s,</h2>
                        <p>Vous avez demandé la réinitialisation de votre mot de passe.</p>
                        <p>Cliquez sur le bouton ci-dessous pour choisir un nouveau mot de passe&nbsp;:</p>
                        <p style="text-align: center; margin: 28px 0;">
                            <a href="%s"
                               style="display: inline-block; padding: 14px 32px; background: #8BA68D;
                                      color: #fff; text-decoration: none; border-radius: 999px;
                                      font-weight: 700;">
                                Réinitialiser mon mot de passe
                            </a>
                        </p>
                        <p style="color: #888; font-size: 0.9em;">Ce lien expire dans 30 minutes.</p>
                        <p style="color: #888; font-size: 0.9em;">
                            Si vous n'êtes pas à l'origine de cette demande, vous pouvez ignorer cet email.
                        </p>
                        <hr style="border: none; border-top: 1px solid #eee; margin: 24px 0;">
                        <p style="color: #aaa; font-size: 0.85em; text-align: center;">CESIZen</p>
                    </div>
                    """.formatted(pseudo, resetLink), true);

            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("Erreur lors de l'envoi de l'email", e);
        }
    }
}