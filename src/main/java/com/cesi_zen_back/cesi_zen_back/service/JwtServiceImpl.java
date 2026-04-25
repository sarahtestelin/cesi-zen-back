package com.cesi_zen_back.cesi_zen_back.service;

import com.cesi_zen_back.cesi_zen_back.entity.AppUser;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    @Value("${security.jwt.secret}")
    private String secret;

    @Value("${security.jwt.issuer}")
    private String issuer;

    @Value("${security.jwt.access-token-validity-minutes}")
    private long accessTokenValidityMinutes;

    @Override
    public String generateAccessToken(AppUser user) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(now.plus(accessTokenValidityMinutes, ChronoUnit.MINUTES))
                .subject(user.getMail())
                .claim("userId", user.getIdUser().toString())
                .claim("pseudo", user.getPseudo())
                .claim("role", user.getRole().getRoleName())
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        NimbusJwtEncoder encoder = new NimbusJwtEncoder(
                new com.nimbusds.jose.jwk.source.ImmutableSecret<>(
                        new SecretKeySpec(secret.getBytes(), "HmacSHA256")
                )
        );

        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}