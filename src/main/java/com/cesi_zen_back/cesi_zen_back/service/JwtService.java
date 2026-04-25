package com.cesi_zen_back.cesi_zen_back.service;

import com.cesi_zen_back.cesi_zen_back.entity.AppUser;

public interface JwtService {
    String generateAccessToken(AppUser user);
}