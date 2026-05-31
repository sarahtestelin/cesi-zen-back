package com.cesi_zen_back.cesi_zen_back.config;

import com.cesi_zen_back.cesi_zen_back.exception.BadRequestException;
import com.cesi_zen_back.cesi_zen_back.service.RateLimitService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

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
        String clientIp = request.getRemoteAddr();

        try {
            if (path.startsWith("/api/auth/login") || path.startsWith("/api/auth/register")) {
                rateLimitService.checkRateLimit("auth:" + clientIp, 5, 15);
                filterChain.doFilter(request, response);
                return;
            }

            if (path.startsWith("/api/auth/refresh")) {
                rateLimitService.checkRateLimit("refresh:" + clientIp, 10, 1);
                filterChain.doFilter(request, response);
                return;
            }

            if (
                    path.startsWith("/api/v1/diagnostics/anonymous")
                            || path.startsWith("/api/v1/diagnostics/questions")
                            || path.startsWith("/api/v1/ressources")
                            || path.startsWith("/api/csrf")
            ) {
                filterChain.doFilter(request, response);
                return;
            }

            rateLimitService.checkRateLimit(clientIp, 100, 1);
            filterChain.doFilter(request, response);

        } catch (BadRequestException e) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"error\":\"Trop de tentatives. Veuillez réessayer plus tard.\"}");
        }
    }
}
