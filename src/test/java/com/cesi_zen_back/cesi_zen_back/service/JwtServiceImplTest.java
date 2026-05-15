package com.cesi_zen_back.cesi_zen_back.service;

import com.cesi_zen_back.cesi_zen_back.entity.AppUser;
import com.cesi_zen_back.cesi_zen_back.entity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceImplTest {

    private JwtServiceImpl jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtServiceImpl();

        ReflectionTestUtils.setField(jwtService, "secret", "long-random-secret-for-cesizen-project-test-123456789");
        ReflectionTestUtils.setField(jwtService, "issuer", "cesi-zen-back-test");
        ReflectionTestUtils.setField(jwtService, "accessTokenValidityMinutes", 15L);
    }

    @Test
    void generateAccessToken_shouldReturnNonBlankToken() {
        Role role = Role.builder()
                .roleId(UUID.randomUUID())
                .roleName("USER")
                .build();

        AppUser user = AppUser.builder()
                .idUser(UUID.randomUUID())
                .mail("user@test.fr")
                .pseudo("Sarah")
                .role(role)
                .build();

        String token = jwtService.generateAccessToken(user);

        assertThat(token).isNotBlank();
    }
}
