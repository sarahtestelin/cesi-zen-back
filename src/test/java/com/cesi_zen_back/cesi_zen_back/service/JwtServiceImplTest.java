package com.cesi_zen_back.cesi_zen_back.service;

import com.cesi_zen_back.cesi_zen_back.entity.AppUser;
import com.cesi_zen_back.cesi_zen_back.entity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.spec.SecretKeySpec;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceImplTest {

    private static final String SECRET = "long-random-secret-for-cesizen-project-test-123456789";

    private JwtServiceImpl jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtServiceImpl();

        ReflectionTestUtils.setField(jwtService, "secret", SECRET);
        ReflectionTestUtils.setField(jwtService, "issuer", "cesi-zen-back-test");
        ReflectionTestUtils.setField(jwtService, "accessTokenValidityMinutes", 15L);
    }

    @Test
    void generateAccessToken_shouldContainExpectedClaims() {
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

        Jwt decodedToken = NimbusJwtDecoder
                .withSecretKey(new SecretKeySpec(SECRET.getBytes(), "HmacSHA256"))
                .build()
                .decode(token);

        assertThat(token).isNotBlank();
        assertThat(decodedToken.getSubject()).isEqualTo("user@test.fr");
        assertThat(decodedToken.getIssuer().toString()).isEqualTo("cesi-zen-back-test");
        assertThat(decodedToken.getClaimAsString("userId")).isEqualTo(user.getIdUser().toString());
        assertThat(decodedToken.getClaimAsString("pseudo")).isEqualTo("Sarah");
        assertThat(decodedToken.getClaimAsString("role")).isEqualTo("USER");
        assertThat(decodedToken.getExpiresAt()).isAfter(decodedToken.getIssuedAt());
    }
}