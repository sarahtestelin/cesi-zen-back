package com.cesi_zen_back.cesi_zen_back.service;

import com.cesi_zen_back.cesi_zen_back.entity.AdminAuditLog;
import com.cesi_zen_back.cesi_zen_back.repository.AdminAuditLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AdminAuditServiceImplTest {

    @Mock
    private AdminAuditLogRepository adminAuditLogRepository;

    @InjectMocks
    private AdminAuditServiceImpl service;

    @Test
    void log_shouldHashAdminMailAndRedactSensitiveDetails() {
        service.log(
                "Admin@Test.FR",
                "UPDATE_USER",
                "APP_USER",
                "123",
                "mail=test@test.fr password:Secret123 token=abc"
        );

        ArgumentCaptor<AdminAuditLog> captor = ArgumentCaptor.forClass(AdminAuditLog.class);
        verify(adminAuditLogRepository).save(captor.capture());

        AdminAuditLog log = captor.getValue();

        assertThat(log.getAdminMail()).startsWith("ADMIN_SHA256_");
        assertThat(log.getAdminMail()).doesNotContain("Admin@Test.FR");
        assertThat(log.getAction()).isEqualTo("UPDATE_USER");
        assertThat(log.getTargetType()).isEqualTo("APP_USER");
        assertThat(log.getTargetId()).isEqualTo("123");
        assertThat(log.getDetails()).contains("[EMAIL_REDACTED]");
        assertThat(log.getDetails()).contains("password=[REDACTED]");
        assertThat(log.getDetails()).contains("token=[REDACTED]");
    }

    @Test
    void log_shouldUseUnknownAdmin_whenMailIsBlank() {
        service.log(" ", "ACTION", "TYPE", null, null);

        ArgumentCaptor<AdminAuditLog> captor = ArgumentCaptor.forClass(AdminAuditLog.class);
        verify(adminAuditLogRepository).save(captor.capture());

        assertThat(captor.getValue().getAdminMail()).isEqualTo("ADMIN_UNKNOWN");
        assertThat(captor.getValue().getDetails()).isNull();
    }
}