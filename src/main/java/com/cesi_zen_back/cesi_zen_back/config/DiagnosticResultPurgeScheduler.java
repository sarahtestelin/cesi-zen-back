package com.cesi_zen_back.cesi_zen_back.config;

import com.cesi_zen_back.cesi_zen_back.service.DiagnosticResultPurgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DiagnosticResultPurgeScheduler {

    private final DiagnosticResultPurgeService diagnosticResultPurgeService;

    @Scheduled(cron = "${app.diagnostic.purge-cron:0 30 2 * * *}")
    public void purgeOldDiagnosticResultsEveryNight() {
        long deletedCount = diagnosticResultPurgeService.purgeOldDiagnosticResults();
        System.out.println("Purge diagnostic exécutée - résultats supprimés : " + deletedCount);
    }
}