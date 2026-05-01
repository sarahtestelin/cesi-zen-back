package com.cesi_zen_back.cesi_zen_back.service;

import com.cesi_zen_back.cesi_zen_back.exception.BadRequestException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RateLimitServiceImplTest {

    private final RateLimitServiceImpl service = new RateLimitServiceImpl();

    @Test
    void checkRateLimit_shouldAllowAttemptsBelowLimit() {
        service.checkRateLimit("login:127.0.0.1", 3, 15);
        service.checkRateLimit("login:127.0.0.1", 3, 15);
        service.checkRateLimit("login:127.0.0.1", 3, 15);
    }

    @Test
    void checkRateLimit_shouldRejectAttemptAboveLimit() {
        service.checkRateLimit("login:blocked", 2, 15);
        service.checkRateLimit("login:blocked", 2, 15);

        assertThatThrownBy(() -> service.checkRateLimit("login:blocked", 2, 15))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Trop de tentatives. Veuillez réessayer plus tard.");
    }
}