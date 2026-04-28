package com.cesi_zen_back.cesi_zen_back.service;

public interface EmailService {

    void sendResetPasswordEmail(String to, String pseudo, String resetLink);
}