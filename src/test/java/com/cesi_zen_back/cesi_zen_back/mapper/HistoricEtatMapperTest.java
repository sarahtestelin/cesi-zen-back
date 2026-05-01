package com.cesi_zen_back.cesi_zen_back.mapper;

import com.cesi_zen_back.cesi_zen_back.dto.HistoricEtatResponseDto;
import com.cesi_zen_back.cesi_zen_back.entity.HistoricEtat;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class HistoricEtatMapperTest {

    @Test
    void toDto_shouldMapHistoryFields() {
        UUID id = UUID.randomUUID();
        UUID entityId = UUID.randomUUID();
        LocalDateTime modificationDate = LocalDateTime.now();

        HistoricEtat history = new HistoricEtat();
        history.setId(id);
        history.setOldValue("old");
        history.setNewValue("new");
        history.setComment("UPDATE");
        history.setEntityType("RESSOURCE");
        history.setEntityId(entityId);
        history.setModificationDate(modificationDate);

        HistoricEtatResponseDto dto = HistoricEtatMapper.toDto(history);

        assertThat(dto.id()).isEqualTo(id);
        assertThat(dto.oldValue()).isEqualTo("old");
        assertThat(dto.newValue()).isEqualTo("new");
        assertThat(dto.comment()).isEqualTo("UPDATE");
        assertThat(dto.entityType()).isEqualTo("RESSOURCE");
        assertThat(dto.entityId()).isEqualTo(entityId);
        assertThat(dto.modificationDate()).isEqualTo(modificationDate);
    }
}