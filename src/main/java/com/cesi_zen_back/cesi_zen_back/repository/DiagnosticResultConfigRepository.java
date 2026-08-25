package com.cesi_zen_back.cesi_zen_back.repository;

import com.cesi_zen_back.cesi_zen_back.entity.DiagnosticResultConfig;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiagnosticResultConfigRepository
    extends JpaRepository<DiagnosticResultConfig, UUID> {

  Optional<DiagnosticResultConfig>
      findFirstByActiveTrueAndMinScoreLessThanEqualAndMaxScoreGreaterThanEqual(
          int minScore, int maxScore);

  List<DiagnosticResultConfig> findByActiveTrueOrderByMinScoreAsc();

  List<DiagnosticResultConfig> findAllByOrderByMinScoreAsc();
}
