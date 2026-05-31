package com.cesi_zen_back.cesi_zen_back.controller;

import com.cesi_zen_back.cesi_zen_back.dto.ChangePasswordDto;
import com.cesi_zen_back.cesi_zen_back.dto.ForgotPasswordDto;
import com.cesi_zen_back.cesi_zen_back.dto.ResetPasswordDto;
import com.cesi_zen_back.cesi_zen_back.service.PasswordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PasswordControllerTest {

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

        verify(passwordService).requestResetPassword(any(ForgotPasswordDto.class));
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

        verify(passwordService).resetPassword(any(ResetPasswordDto.class));
    }

    @Test
    void changePassword_shouldCallServiceWithAuthenticatedUser() throws Exception {
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

        verify(passwordService).changePassword(eq("user@test.fr"), any(ChangePasswordDto.class));
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
