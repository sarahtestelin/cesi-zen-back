package com.cesi_zen_back.cesi_zen_back.config;

import com.cesi_zen_back.cesi_zen_back.service.RateLimitService;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public RateLimitFilter rateLimitFilter(RateLimitService rateLimitService) {
        return new RateLimitFilter(rateLimitService);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of("http://localhost:4200"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            String role = jwt.getClaimAsString("role");

            if (role == null || role.isBlank()) {
                return java.util.List.of();
            }

            return java.util.List.of(new SimpleGrantedAuthority("ROLE_" + role));
        });

        return converter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            RateLimitFilter rateLimitFilter
    ) throws Exception {

        CsrfTokenRequestAttributeHandler csrfHandler = new CsrfTokenRequestAttributeHandler();
        PathPatternRequestMatcher.Builder matcherBuilder = PathPatternRequestMatcher.withDefaults();

        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(csrfHandler)
                        .requireCsrfProtectionMatcher(new OrRequestMatcher(
                                matcherBuilder.matcher(HttpMethod.POST, "/api/auth/refresh"),
                                matcherBuilder.matcher(HttpMethod.POST, "/api/auth/logout")
                        ))
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(rateLimitFilter, BearerTokenAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/csrf").permitAll()

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

                        .requestMatchers("/api/users/me").authenticated()
                        .requestMatchers("/api/users/me/export").authenticated()

                        .requestMatchers(HttpMethod.GET, "/api/users").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/users/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/users/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/users/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/users/*/disable").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/users/*/enable").hasRole("ADMIN")

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