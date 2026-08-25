package com.cesi_zen_back.cesi_zen_back.service;

import com.cesi_zen_back.cesi_zen_back.repository.DiagnosticResultRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DiagnosticResultPurgeServiceImpl implements DiagnosticResultPurgeService {

  private final DiagnosticResultRepository diagnosticResultRepository;

  @Value("${app.diagnostic.retention-days:365}")
  private int diagnosticRetentionDays;

  @Override
  @Transactional
  public long purgeOldDiagnosticResults() {
    LocalDateTime limitDate = LocalDateTime.now().minusDays(diagnosticRetentionDays);
    return diagnosticResultRepository.deleteByCreatedAtBefore(limitDate);
  }
}
