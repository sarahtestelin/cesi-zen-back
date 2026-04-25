package com.cesi_zen_back.cesi_zen_back.service;

import com.cesi_zen_back.cesi_zen_back.dto.ChangePasswordDto;
import com.cesi_zen_back.cesi_zen_back.dto.ForgotPasswordDto;
import com.cesi_zen_back.cesi_zen_back.dto.ResetPasswordDto;

public interface PasswordService {
    void requestResetPassword(ForgotPasswordDto dto);
    void resetPassword(ResetPasswordDto dto);
    void changePassword(String mail, ChangePasswordDto dto);
}