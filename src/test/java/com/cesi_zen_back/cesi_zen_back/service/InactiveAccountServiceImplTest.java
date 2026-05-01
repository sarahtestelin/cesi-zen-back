package com.cesi_zen_back.cesi_zen_back.service;

import com.cesi_zen_back.cesi_zen_back.entity.AppUser;
import com.cesi_zen_back.cesi_zen_back.entity.Role;
import com.cesi_zen_back.cesi_zen_back.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InactiveAccountServiceImplTest {

    @Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private InactiveAccountServiceImpl service;

    @Test
    void disableInactiveAccounts_shouldDisableOnlyNonAdminUsers() {
        ReflectionTestUtils.setField(service, "inactivityDays", 365L);

        AppUser user = AppUser.builder()
                .mail("user@test.fr")
                .isActive(true)
                .role(Role.builder().roleName("USER").build())
                .build();

        AppUser admin = AppUser.builder()
                .mail("admin@test.fr")
                .isActive(true)
                .role(Role.builder().roleName("ADMIN").build())
                .build();

        when(appUserRepository.findByIsActiveTrueAndLastConnexionBefore(any()))
                .thenReturn(List.of(user, admin));

        service.disableInactiveAccounts();

        assertThat(user.isActive()).isFalse();
        assertThat(admin.isActive()).isTrue();

        verify(appUserRepository).saveAll(List.of(user, admin));
    }
}