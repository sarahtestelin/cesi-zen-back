package com.cesi_zen_back.cesi_zen_back.service;

import com.cesi_zen_back.cesi_zen_back.dto.HistoricEtatResponseDto;
import com.cesi_zen_back.cesi_zen_back.dto.RessourceRequestDto;
import com.cesi_zen_back.cesi_zen_back.dto.RessourceResponseDto;

import java.util.List;
import java.util.UUID;

public interface RessourceService {

    List<RessourceResponseDto> listPublic(String search, String category);

    RessourceResponseDto getPublic(UUID id);

    List<RessourceResponseDto> listAdmin(String search, String category, Boolean active);

    RessourceResponseDto getAdmin(UUID id);

    RessourceResponseDto create(RessourceRequestDto dto);

    RessourceResponseDto update(UUID id, RessourceRequestDto dto);

    RessourceResponseDto enable(UUID id);

    RessourceResponseDto disable(UUID id);

    void delete(UUID id);

    List<HistoricEtatResponseDto> history(UUID id);
}