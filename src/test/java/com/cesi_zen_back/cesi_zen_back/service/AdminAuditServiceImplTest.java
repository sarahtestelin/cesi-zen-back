package com.cesi_zen_back.cesi_zen_back.service;

import com.cesi_zen_back.cesi_zen_back.entity.AdminAuditLog;
import com.cesi_zen_back.cesi_zen_back.repository.AdminAuditLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AdminAuditServiceImplTest {

    @Mock
    private AdminAuditLogRepository adminAuditLogRepository;

    @InjectMocks
    private AdminAuditServiceImpl service;

    @Test
    void log_shouldSaveAuditLogEntry() {
        service.log("admin@test.fr", "UPDATE_USER", "APP_USER", "123", "modification du pseudo");

        verify(adminAuditLogRepository).save(any(AdminAuditLog.class));
    }

    @Test
    void log_shouldHandleBlankMail() {
        service.log(" ", "ACTION", "TYPE", null, null);

        verify(adminAuditLogRepository).save(any(AdminAuditLog.class));
    }
}
