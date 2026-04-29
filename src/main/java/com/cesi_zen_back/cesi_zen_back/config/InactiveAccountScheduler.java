package com.cesi_zen_back.cesi_zen_back.config;

import com.cesi_zen_back.cesi_zen_back.service.InactiveAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InactiveAccountScheduler {

    private final InactiveAccountService inactiveAccountService;

    @Scheduled(cron = "0 0 2 * * *")
    public void disableInactiveAccountsEveryNight() {
        inactiveAccountService.disableInactiveAccounts();
    }
}