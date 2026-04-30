package com.cesi_zen_back.cesi_zen_back.controller;

import com.cesi_zen_back.cesi_zen_back.dto.AppUserDto;
import com.cesi_zen_back.cesi_zen_back.dto.AuthResponseDto;
import com.cesi_zen_back.cesi_zen_back.dto.LoginDto;
import com.cesi_zen_back.cesi_zen_back.dto.RegisterUserDto;
import com.cesi_zen_back.cesi_zen_back.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerFunctionalTest {

    private MockMvc mockMvc;
    private AuthService authService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(new AuthController(authService))
                .setValidator(validator)
                .build();
    }

    @Test
    void register_shouldReturnAccessTokenUserAndSecureRefreshCookie() throws Exception {
        AppUserDto user = new AppUserDto(
                UUID.randomUUID(),
                "user@test.fr",
                "Sarah",
                true,
                LocalDateTime.now(),
                "USER"
        );

        when(authService.register(any(RegisterUserDto.class)))
                .thenReturn(new AuthResponseDto("access-token", "refresh-token", user));

        String body = """
                {
                  "mail": "user@test.fr",
                  "pseudo": "Sarah",
                  "password": "Password123!",
                  "deviceInfo": "Terminal"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andExpect(jsonPath("$.user.mail").value("user@test.fr"))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("refreshToken=refresh-token")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("HttpOnly")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("Secure")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("SameSite=Strict")));

        ArgumentCaptor<RegisterUserDto> captor = ArgumentCaptor.forClass(RegisterUserDto.class);
        verify(authService).register(captor.capture());

        assertThat(captor.getValue().mail()).isEqualTo("user@test.fr");
        assertThat(captor.getValue().pseudo()).isEqualTo("Sarah");
        assertThat(captor.getValue().deviceInfo()).isEqualTo("Terminal");
    }

    @Test
    void register_shouldRejectInvalidBody() throws Exception {
        String body = """
                {
                  "mail": "not-an-email",
                  "pseudo": "Sa",
                  "password": "short"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(any());
    }

    @Test
    void login_shouldReturnAccessTokenAndRefreshCookie() throws Exception {
        AppUserDto user = new AppUserDto(
                UUID.randomUUID(),
                "user@test.fr",
                "Sarah",
                true,
                LocalDateTime.now(),
                "USER"
        );

        when(authService.login(any(LoginDto.class)))
                .thenReturn(new AuthResponseDto("access-token", "refresh-token", user));

        String body = """
                {
                  "mail": "user@test.fr",
                  "password": "Password123!",
                  "deviceInfo": "Terminal"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andExpect(jsonPath("$.user.pseudo").value("Sarah"))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("refreshToken=refresh-token")));

        verify(authService).login(any(LoginDto.class));
    }

    @Test
    void login_shouldRejectInvalidMail() throws Exception {
        String body = """
                {
                  "mail": "bad-mail",
                  "password": "Password123!"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any());
    }

    @Test
    void refresh_shouldUseRefreshCookieAndReturnNewAccessToken() throws Exception {
        AppUserDto user = new AppUserDto(
                UUID.randomUUID(),
                "user@test.fr",
                "Sarah",
                true,
                LocalDateTime.now(),
                "USER"
        );

        when(authService.refresh("old-refresh-token"))
                .thenReturn(new AuthResponseDto("new-access-token", "new-refresh-token", user));

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new jakarta.servlet.http.Cookie("refreshToken", "old-refresh-token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("refreshToken=new-refresh-token")));

        verify(authService).refresh("old-refresh-token");
    }

    @Test
    void logout_shouldCallServiceAndDeleteRefreshCookie() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .cookie(new jakarta.servlet.http.Cookie("refreshToken", "refresh-token")))
                .andExpect(status().isNoContent())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("refreshToken=")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("Max-Age=0")));

        verify(authService).logout("refresh-token");
    }
}