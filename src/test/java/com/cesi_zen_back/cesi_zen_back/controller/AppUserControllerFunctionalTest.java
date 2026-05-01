package com.cesi_zen_back.cesi_zen_back.controller;

import com.cesi_zen_back.cesi_zen_back.dto.*;
import com.cesi_zen_back.cesi_zen_back.service.AppUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.core.MethodParameter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AppUserControllerFunctionalTest {

    private MockMvc mockMvc;
    private AppUserService appUserService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private UUID userId;
    private Jwt jwt;

    @BeforeEach
    void setUp() {
        appUserService = mock(AppUserService.class);
        userId = UUID.randomUUID();

        jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("user@test.fr")
                .build();

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        MappingJackson2HttpMessageConverter jacksonConverter =
                new MappingJackson2HttpMessageConverter(objectMapper);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new AppUserController(appUserService))
                .setValidator(validator)
                .setMessageConverters(jacksonConverter)
                .setCustomArgumentResolvers(jwtArgumentResolver())
                .build();
    }

    @Test
    void getCurrentUser_shouldReturnAuthenticatedUser() throws Exception {
        AppUserDto dto = new AppUserDto(
                userId,
                "user@test.fr",
                "Sarah",
                true,
                LocalDateTime.now(),
                "USER"
        );

        when(appUserService.getCurrentUser("user@test.fr")).thenReturn(dto);

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.mail").value("user@test.fr"))
                .andExpect(jsonPath("$.pseudo").value("Sarah"));

        verify(appUserService).getCurrentUser("user@test.fr");
    }

    @Test
    void exportCurrentUserData_shouldReturnExport() throws Exception {
        UserDataExportDto export = new UserDataExportDto(
                userId,
                "user@test.fr",
                "Sarah",
                true,
                LocalDateTime.now(),
                "USER",
                List.of(new DiagnosticResultResponseDto(
                        UUID.randomUUID(),
                        150,
                        "MODERE",
                        "Stress modéré",
                        LocalDateTime.now()
                )),
                List.of(),
                List.of()
        );

        when(appUserService.exportCurrentUserData("user@test.fr")).thenReturn(export);

        mockMvc.perform(get("/api/users/me/export"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.mail").value("user@test.fr"))
                .andExpect(jsonPath("$.diagnosticResults[0].finalScore").value(150));

        verify(appUserService).exportCurrentUserData("user@test.fr");
    }

    @Test
    void updateCurrentUser_shouldValidateAndCallService() throws Exception {
        AppUserDto response = new AppUserDto(
                userId,
                "new@test.fr",
                "NewSarah",
                true,
                LocalDateTime.now(),
                "USER"
        );

        when(appUserService.updateCurrentUser(eq("user@test.fr"), any(UpdateCurrentUserDto.class)))
                .thenReturn(response);

        String body = """
                {
                  "mail": "new@test.fr",
                  "pseudo": "NewSarah"
                }
                """;

        mockMvc.perform(put("/api/users/me")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mail").value("new@test.fr"))
                .andExpect(jsonPath("$.pseudo").value("NewSarah"));

        ArgumentCaptor<UpdateCurrentUserDto> captor = ArgumentCaptor.forClass(UpdateCurrentUserDto.class);
        verify(appUserService).updateCurrentUser(eq("user@test.fr"), captor.capture());

        assertThat(captor.getValue().mail()).isEqualTo("new@test.fr");
        assertThat(captor.getValue().pseudo()).isEqualTo("NewSarah");
    }

    @Test
    void updateCurrentUser_shouldRejectInvalidMail() throws Exception {
        String body = """
                {
                  "mail": "bad-mail",
                  "pseudo": "Sarah"
                }
                """;

        mockMvc.perform(put("/api/users/me")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest());

        verify(appUserService, never()).updateCurrentUser(anyString(), any());
    }

    @Test
    void anonymizeCurrentUser_shouldReturnOkAndCallService() throws Exception {
        mockMvc.perform(delete("/api/users/me"))
                .andExpect(status().isOk());

        verify(appUserService).anonymizeCurrentUser("user@test.fr");
    }

    @Test
    void getAllUsers_shouldReturnUsers() throws Exception {
        AppUserDto dto = new AppUserDto(
                userId,
                "user@test.fr",
                "Sarah",
                true,
                LocalDateTime.now(),
                "USER"
        );

        when(appUserService.getAllUsers()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(userId.toString()))
                .andExpect(jsonPath("$[0].mail").value("user@test.fr"));

        verify(appUserService).getAllUsers();
    }

    @Test
    void getUserById_shouldReturnUser() throws Exception {
        AppUserDto dto = new AppUserDto(
                userId,
                "user@test.fr",
                "Sarah",
                true,
                LocalDateTime.now(),
                "USER"
        );

        when(appUserService.getUserById(userId)).thenReturn(dto);

        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.pseudo").value("Sarah"));

        verify(appUserService).getUserById(userId);
    }

    @Test
    void updateUser_shouldForwardAdminMail() throws Exception {
        AppUserDto response = new AppUserDto(
                userId,
                "updated@test.fr",
                "Updated",
                true,
                LocalDateTime.now(),
                "USER"
        );

        when(appUserService.updateUser(eq(userId), any(AppUserDto.class), eq("user@test.fr")))
                .thenReturn(response);

        String body = """
                {
                  "id": "%s",
                  "mail": "updated@test.fr",
                  "pseudo": "Updated",
                  "appUserIsActive": true,
                  "lastConnectionAt": "2026-04-30T10:00:00",
                  "role": "USER"
                }
                """.formatted(userId);

        mockMvc.perform(put("/api/users/{id}", userId)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mail").value("updated@test.fr"))
                .andExpect(jsonPath("$.pseudo").value("Updated"));

        verify(appUserService).updateUser(eq(userId), any(AppUserDto.class), eq("user@test.fr"));
    }

    @Test
    void deleteUser_shouldCallServiceWithAdminMail() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", userId))
                .andExpect(status().isOk());

        verify(appUserService).deleteUser(userId, "user@test.fr");
    }

    @Test
    void disableUser_shouldReturnDisabledUser() throws Exception {
        AppUserDto response = new AppUserDto(
                userId,
                "user@test.fr",
                "Sarah",
                false,
                LocalDateTime.now(),
                "USER"
        );

        when(appUserService.disableUser(userId, "user@test.fr")).thenReturn(response);

        mockMvc.perform(patch("/api/users/{id}/disable", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appUserIsActive").value(false));

        verify(appUserService).disableUser(userId, "user@test.fr");
    }

    @Test
    void enableUser_shouldReturnEnabledUser() throws Exception {
        AppUserDto response = new AppUserDto(
                userId,
                "user@test.fr",
                "Sarah",
                true,
                LocalDateTime.now(),
                "USER"
        );

        when(appUserService.enableUser(userId, "user@test.fr")).thenReturn(response);

        mockMvc.perform(patch("/api/users/{id}/enable", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appUserIsActive").value(true));

        verify(appUserService).enableUser(userId, "user@test.fr");
    }

    private HandlerMethodArgumentResolver jwtArgumentResolver() {
        return new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return Jwt.class.isAssignableFrom(parameter.getParameterType());
            }

            @Override
            public Object resolveArgument(
                    MethodParameter parameter,
                    ModelAndViewContainer mavContainer,
                    NativeWebRequest webRequest,
                    WebDataBinderFactory binderFactory
            ) {
                return jwt;
            }
        };
    }
}