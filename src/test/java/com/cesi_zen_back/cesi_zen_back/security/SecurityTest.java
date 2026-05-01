package com.cesi_zen_back.cesi_zen_back.security;

import com.cesi_zen_back.cesi_zen_back.repository.RefreshTokenRepository;
import com.cesi_zen_back.cesi_zen_back.service.RateLimitService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class SecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RateLimitService rateLimitService;

    @MockBean
    private RefreshTokenRepository refreshTokenRepository;

    @Test
    void protectedUsersEndpoint_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void publicRegisterEndpoint_shouldStayAccessibleWithoutAuthentication() throws Exception {
        String invalidBody = """
                {
                  "mail": "not-an-email",
                  "pseudo": "Sa",
                  "password": "short",
                  "deviceInfo": "JUnit"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void publicRessourcesEndpoint_shouldStayAccessibleWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/ressources"))
                .andExpect(status().isOk());
    }

    @Test
    void swaggerEndpoint_shouldStayPublic() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }
}