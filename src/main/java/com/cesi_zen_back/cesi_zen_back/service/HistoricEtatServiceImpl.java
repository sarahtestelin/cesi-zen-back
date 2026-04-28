package com.cesi_zen_back.cesi_zen_back.service;

import com.cesi_zen_back.cesi_zen_back.dto.HistoricEtatResponseDto;
import com.cesi_zen_back.cesi_zen_back.mapper.HistoricEtatMapper;
import com.cesi_zen_back.cesi_zen_back.repository.HistoricEtatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HistoricEtatServiceImpl implements HistoricEtatService {

    private final HistoricEtatRepository historicEtatRepository;

    @Override
    public List<HistoricEtatResponseDto> getAllHistory() {
        return historicEtatRepository.findAll()
                .stream()
                .map(HistoricEtatMapper::toDto)
                .toList();
    }

    @Override
    public List<HistoricEtatResponseDto> getHistoryByEntity(String entityType, UUID entityId) {
        return historicEtatRepository.findAll()
                .stream()
                .filter(historicEtat -> entityType.equals(historicEtat.getEntityType()))
                .filter(historicEtat -> entityId.equals(historicEtat.getEntityId()))
                .map(HistoricEtatMapper::toDto)
                .toList();
    }
}