package com.cesi_zen_back.cesi_zen_back.service;

import com.cesi_zen_back.cesi_zen_back.entity.AppUser;
import com.cesi_zen_back.cesi_zen_back.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InactiveAccountServiceImpl implements InactiveAccountService {

    private final AppUserRepository appUserRepository;

    @Value("${app.account.inactivity-days}")
    private long inactivityDays;

    @Override
    @Transactional
    public void disableInactiveAccounts() {
        LocalDateTime limitDate = LocalDateTime.now().minusDays(inactivityDays);

        List<AppUser> inactiveUsers = appUserRepository.findByIsActiveTrueAndLastConnexionBefore(limitDate);

        inactiveUsers.stream()
                .filter(user -> user.getRole() == null || !"ADMIN".equals(user.getRole().getRoleName()))
                .forEach(user -> user.setActive(false));

        appUserRepository.saveAll(inactiveUsers);
    }
}