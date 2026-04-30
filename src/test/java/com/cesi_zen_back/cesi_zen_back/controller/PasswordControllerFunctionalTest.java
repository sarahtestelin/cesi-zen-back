package com.cesi_zen_back.cesi_zen_back.controller;

import com.cesi_zen_back.cesi_zen_back.dto.ChangePasswordDto;
import com.cesi_zen_back.cesi_zen_back.dto.ForgotPasswordDto;
import com.cesi_zen_back.cesi_zen_back.dto.ResetPasswordDto;
import com.cesi_zen_back.cesi_zen_back.service.PasswordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PasswordControllerFunctionalTest {

    private MockMvc mockMvc;
    private PasswordService passwordService;

    @BeforeEach
    void setUp() {
        passwordService = mock(PasswordService.class);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(new PasswordController(passwordService))
                .setValidator(validator)
                .build();
    }

    @Test
    void requestResetPassword_shouldReturnOkAndCallService() throws Exception {
        String body = """
                {
                  "mail": "user@test.fr"
                }
                """;

        mockMvc.perform(post("/api/password/reset-request")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk());

        ArgumentCaptor<ForgotPasswordDto> captor = ArgumentCaptor.forClass(ForgotPasswordDto.class);
        verify(passwordService).requestResetPassword(captor.capture());

        assertThat(captor.getValue().mail()).isEqualTo("user@test.fr");
    }

    @Test
    void requestResetPassword_shouldRejectInvalidMail() throws Exception {
        String body = """
                {
                  "mail": "bad-mail"
                }
                """;

        mockMvc.perform(post("/api/password/reset-request")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest());

        verify(passwordService, never()).requestResetPassword(any());
    }

    @Test
    void resetPassword_shouldReturnOkAndCallService() throws Exception {
        String body = """
                {
                  "token": "valid-token",
                  "newPassword": "NewPassword123!"
                }
                """;

        mockMvc.perform(post("/api/password/reset")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk());

        ArgumentCaptor<ResetPasswordDto> captor = ArgumentCaptor.forClass(ResetPasswordDto.class);
        verify(passwordService).resetPassword(captor.capture());

        assertThat(captor.getValue().token()).isEqualTo("valid-token");
        assertThat(captor.getValue().newPassword()).isEqualTo("NewPassword123!");
    }

    @Test
    void resetPassword_shouldRejectTooShortPassword() throws Exception {
        String body = """
                {
                  "token": "valid-token",
                  "newPassword": "short"
                }
                """;

        mockMvc.perform(post("/api/password/reset")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest());

        verify(passwordService, never()).resetPassword(any());
    }

    @Test
    void changePassword_shouldUseAuthenticatedUserMail() throws Exception {
        String body = """
                {
                  "currentPassword": "OldPassword123!",
                  "newPassword": "NewPassword123!"
                }
                """;

        TestingAuthenticationToken authentication =
                new TestingAuthenticationToken("user@test.fr", null);

        mockMvc.perform(post("/api/password/change")
                        .principal(authentication)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk());

        ArgumentCaptor<ChangePasswordDto> captor = ArgumentCaptor.forClass(ChangePasswordDto.class);
        verify(passwordService).changePassword(eq("user@test.fr"), captor.capture());

        assertThat(captor.getValue().currentPassword()).isEqualTo("OldPassword123!");
        assertThat(captor.getValue().newPassword()).isEqualTo("NewPassword123!");
    }

    @Test
    void changePassword_shouldRejectInvalidBody() throws Exception {
        String body = """
                {
                  "currentPassword": "",
                  "newPassword": "short"
                }
                """;

        TestingAuthenticationToken authentication =
                new TestingAuthenticationToken("user@test.fr", null);

        mockMvc.perform(post("/api/password/change")
                        .principal(authentication)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest());

        verify(passwordService, never()).changePassword(anyString(), any());
    }
}