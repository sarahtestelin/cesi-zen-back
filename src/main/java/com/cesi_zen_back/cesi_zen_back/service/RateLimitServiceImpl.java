package com.cesi_zen_back.cesi_zen_back.service;

import com.cesi_zen_back.cesi_zen_back.exception.BadRequestException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitServiceImpl implements RateLimitService {

    private final Map<String, RateLimitEntry> attempts = new ConcurrentHashMap<>();

    @Override
    public void checkRateLimit(String key, int maxAttempts, long windowInMinutes) {
        LocalDateTime now = LocalDateTime.now();

        RateLimitEntry entry = attempts.get(key);

        if (entry == null || entry.windowStart().plusMinutes(windowInMinutes).isBefore(now)) {
            attempts.put(key, new RateLimitEntry(1, now));
            return;
        }

        if (entry.count() >= maxAttempts) {
            throw new BadRequestException("Trop de tentatives. Veuillez réessayer plus tard.");
        }

        attempts.put(key, new RateLimitEntry(entry.count() + 1, entry.windowStart()));
    }

    private record RateLimitEntry(
            int count,
            LocalDateTime windowStart
    ) {
    }
}