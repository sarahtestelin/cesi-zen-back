package com.cesi_zen_back.cesi_zen_back.mapper;

import com.cesi_zen_back.cesi_zen_back.dto.HistoricEtatResponseDto;
import com.cesi_zen_back.cesi_zen_back.entity.HistoricEtat;

public class HistoricEtatMapper {

    private HistoricEtatMapper() {
    }

    public static HistoricEtatResponseDto toDto(HistoricEtat historicEtat) {
        return new HistoricEtatResponseDto(
                historicEtat.getId(),
                historicEtat.getOldValue(),
                historicEtat.getNewValue(),
                historicEtat.getComment(),
                historicEtat.getEntityType(),
                historicEtat.getEntityId(),
                historicEtat.getModificationDate()
        );
    }
}