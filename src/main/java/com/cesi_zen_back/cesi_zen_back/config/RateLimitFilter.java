package com.cesi_zen_back.cesi_zen_back.config;

import com.cesi_zen_back.cesi_zen_back.service.RateLimitService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
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

        if (
                path.startsWith("/api/v1/diagnostics/anonymous")
                        || path.startsWith("/api/v1/diagnostics/questions")
                        || path.startsWith("/api/auth/login")
                        || path.startsWith("/api/auth/register")
                        || path.startsWith("/api/auth/refresh")
                        || path.startsWith("/api/v1/ressources")
        ) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = request.getRemoteAddr();

        rateLimitService.checkRateLimit(clientIp, 100, 1);

        filterChain.doFilter(request, response);
    }
}