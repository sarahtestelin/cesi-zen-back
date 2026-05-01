package com.cesi_zen_back.cesi_zen_back.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender javaMailSender;

    @InjectMocks
    private EmailServiceImpl service;

    @Test
    void sendResetPasswordEmail_shouldSendExpectedMessage() {
        service.sendResetPasswordEmail("user@test.fr", "Sarah", "https://reset.test/token");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(javaMailSender).send(captor.capture());

        SimpleMailMessage message = captor.getValue();

        assertThat(message.getFrom()).isEqualTo("no-reply@cesizen.local");
        assertThat(message.getTo()).containsExactly("user@test.fr");
        assertThat(message.getSubject()).isEqualTo("Réinitialisation de votre mot de passe CESIZen");
        assertThat(message.getText()).contains("Bonjour Sarah");
        assertThat(message.getText()).contains("https://reset.test/token");
        assertThat(message.getText()).contains("Ce lien expire dans 30 minutes");
    }
}