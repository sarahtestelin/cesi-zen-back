package com.cesi_zen_back.cesi_zen_back.service;

import com.cesi_zen_back.cesi_zen_back.entity.AdminAuditLog;
import com.cesi_zen_back.cesi_zen_back.repository.AdminAuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

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
                .adminMail(toSafeAdminIdentifier(adminMail))
                .action(action)
                .targetType(targetType)
                .targetId(targetId)
                .details(sanitizeDetails(details))
                .build();

        adminAuditLogRepository.save(log);
    }

    private String toSafeAdminIdentifier(String adminMail) {
        if (adminMail == null || adminMail.isBlank()) {
            return "ADMIN_UNKNOWN";
        }

        return "ADMIN_SHA256_" + sha256(adminMail.trim().toLowerCase());
    }

    private String sanitizeDetails(String details) {
        if (details == null || details.isBlank()) {
            return null;
        }

        return details
                .replaceAll("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}", "[EMAIL_REDACTED]")
                .replaceAll("(?i)(password|token|refreshToken|accessToken)\\s*[:=]\\s*[^\\s,;]+", "$1=[REDACTED]");
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new IllegalStateException("Impossible de générer l'identifiant d'audit admin.", e);
        }
    }
}