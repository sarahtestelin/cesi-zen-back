package com.cesi_zen_back.cesi_zen_back.service;

import com.cesi_zen_back.cesi_zen_back.dto.HistoricEtatResponseDto;

import java.util.List;
import java.util.UUID;

public interface HistoricEtatService {

    List<HistoricEtatResponseDto> getAllHistory();

    List<HistoricEtatResponseDto> getHistoryByEntity(String entityType, UUID entityId);
}