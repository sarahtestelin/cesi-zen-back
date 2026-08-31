package com.cesi_zen_back.cesi_zen_back.repository;

import com.cesi_zen_back.cesi_zen_back.entity.HistoricEtat;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoricEtatRepository extends JpaRepository<HistoricEtat, UUID> {

  List<HistoricEtat> findByAppUserIdUserOrderByModificationDateDesc(UUID appUserId);

  List<HistoricEtat> findByEntityTypeAndEntityIdOrderByModificationDateDesc(
      String entityType, UUID entityId);
}
