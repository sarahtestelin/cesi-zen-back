package com.cesi_zen_back.cesi_zen_back.config;

import javax.crypto.spec.SecretKeySpec;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final RateLimitFilter rateLimitFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            String role = jwt.getClaimAsString("role");

            if (role == null || role.isBlank()) {
                return java.util.List.of();
            }

            return java.util.List.of(
                    new SimpleGrantedAuthority("ROLE_" + role)
            );
        });

        return converter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/refresh").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/logout").permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/password/reset-request").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/password/reset").permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/password/change").authenticated()

                        .requestMatchers(HttpMethod.GET, "/api/users/me").authenticated()

                        .requestMatchers("/api/users/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/v1/ressources/admin").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/ressources/admin/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/ressources/*/history").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/ressources").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/ressources/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/ressources/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/ressources/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/v1/ressources").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/ressources/*").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/v1/diagnostics/questions").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/diagnostics/anonymous").permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/v1/diagnostics/submit").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/diagnostics/results/me").authenticated()

                        .requestMatchers("/api/v1/diagnostics/admin/**").hasRole("ADMIN")

                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                );

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder(@Value("${security.jwt.secret}") String secret) {
        return NimbusJwtDecoder
                .withSecretKey(new SecretKeySpec(secret.getBytes(), "HmacSHA256"))
                .build();
    }
}