package com.cesi_zen_back.cesi_zen_back.service;

public interface AdminAuditService {

    void log(
            String adminMail,
            String action,
            String targetType,
            String targetId,
            String details
    );
}