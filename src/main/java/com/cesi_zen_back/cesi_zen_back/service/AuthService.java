package com.cesi_zen_back.cesi_zen_back.service;

import com.cesi_zen_back.cesi_zen_back.dto.AuthResponseDto;
import com.cesi_zen_back.cesi_zen_back.dto.LoginDto;
import com.cesi_zen_back.cesi_zen_back.dto.RegisterUserDto;

public interface AuthService {
    AuthResponseDto register(RegisterUserDto dto);
    AuthResponseDto login(LoginDto dto);
    AuthResponseDto refresh(String refreshToken);
    void logout(String refreshToken);
}