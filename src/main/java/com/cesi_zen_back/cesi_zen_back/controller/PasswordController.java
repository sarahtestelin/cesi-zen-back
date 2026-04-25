package com.cesi_zen_back.cesi_zen_back.controller;

import com.cesi_zen_back.cesi_zen_back.dto.ChangePasswordDto;
import com.cesi_zen_back.cesi_zen_back.dto.ForgotPasswordDto;
import com.cesi_zen_back.cesi_zen_back.dto.ResetPasswordDto;
import com.cesi_zen_back.cesi_zen_back.service.PasswordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/password")
@RequiredArgsConstructor
public class PasswordController {

    private final PasswordService passwordService;

    @PostMapping("/reset-request")
    public void requestResetPassword(@Valid @RequestBody ForgotPasswordDto dto) {
        passwordService.requestResetPassword(dto);
    }

    @PostMapping("/reset")
    public void resetPassword(@Valid @RequestBody ResetPasswordDto dto) {
        passwordService.resetPassword(dto);
    }

    @PostMapping("/change")
    public void changePassword(
            @Valid @RequestBody ChangePasswordDto dto,
            Authentication authentication
    ) {
        passwordService.changePassword(authentication.getName(), dto);
    }
}