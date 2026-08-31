package com.cesi_zen_back.cesi_zen_back.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.cesi_zen_back.cesi_zen_back.entity.AppUser;
import com.cesi_zen_back.cesi_zen_back.entity.Role;
import com.cesi_zen_back.cesi_zen_back.repository.AppUserRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class InactiveAccountServiceImplTest {

  @Mock private AppUserRepository appUserRepository;

  @InjectMocks private InactiveAccountServiceImpl service;

  @Test
  void disableInactiveAccounts_shouldDisableInactiveUsers() {
    ReflectionTestUtils.setField(service, "inactivityDays", 365L);

    AppUser user =
        AppUser.builder()
            .mail("user@test.fr")
            .isActive(true)
            .role(Role.builder().roleName("USER").build())
            .build();

    when(appUserRepository.findByIsActiveTrueAndLastConnexionBefore(any()))
        .thenReturn(List.of(user));

    service.disableInactiveAccounts();

    verify(appUserRepository).saveAll(any());
  }
}
