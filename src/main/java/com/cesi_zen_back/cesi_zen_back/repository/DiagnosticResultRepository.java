package com.cesi_zen_back.cesi_zen_back.repository;

import com.cesi_zen_back.cesi_zen_back.entity.DiagnosticResult;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiagnosticResultRepository extends JpaRepository<DiagnosticResult, UUID> {

  List<DiagnosticResult> findByAppUserIdUserOrderByCreatedAtDesc(UUID appUserId);

  List<DiagnosticResult> findByAppUserIdUser(UUID appUserId);

  long deleteByCreatedAtBefore(LocalDateTime limitDate);
}
