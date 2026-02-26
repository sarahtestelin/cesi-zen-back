package com.cesi_zen_back.cesi_zen_back.repository;

import com.cesi_zen_back.cesi_zen_back.entity.HistoricEtat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface HistoricEtatRepository extends JpaRepository<HistoricEtat, UUID> {}