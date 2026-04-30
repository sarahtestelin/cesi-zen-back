package com.cesi_zen_back.cesi_zen_back.service;

import com.cesi_zen_back.cesi_zen_back.entity.AdminAuditLog;
import com.cesi_zen_back.cesi_zen_back.repository.AdminAuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminAuditServiceImpl implements AdminAuditService {

    private final AdminAuditLogRepository adminAuditLogRepository;

    @Override
    public void log(
            String adminMail,
            String action,
            String targetType,
            String targetId,
            String details
    ) {
        AdminAuditLog log = AdminAuditLog.builder()
                .adminMail(adminMail)
                .action(action)
                .targetType(targetType)
                .targetId(targetId)
                .details(details)
                .build();

        adminAuditLogRepository.save(log);
    }
}