package com.cesi_zen_back.cesi_zen_back.config;

import com.cesi_zen_back.cesi_zen_back.service.RateLimitService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();
        String ip = getClientIp(request);

        try {
            if (HttpMethod.POST.matches(method) && "/api/auth/login".equals(path)) {
                rateLimitService.checkRateLimit("LOGIN:" + ip, 5, 15);
            }

            if (HttpMethod.POST.matches(method) && "/api/password/reset-request".equals(path)) {
                rateLimitService.checkRateLimit("RESET_PASSWORD:" + ip, 3, 15);
            }

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            response.setStatus(429);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"message\":\"Trop de tentatives. Veuillez réessayer plus tard.\"}");
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");

        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }
}