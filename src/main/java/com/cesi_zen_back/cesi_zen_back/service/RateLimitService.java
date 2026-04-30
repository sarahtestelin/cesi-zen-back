package com.cesi_zen_back.cesi_zen_back.service;

public interface RateLimitService {

    void checkRateLimit(String key, int maxAttempts, long windowInMinutes);
}