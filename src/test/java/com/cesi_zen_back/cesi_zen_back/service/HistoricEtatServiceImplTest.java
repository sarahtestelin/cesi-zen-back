package com.cesi_zen_back.cesi_zen_back.service;

import com.cesi_zen_back.cesi_zen_back.dto.HistoricEtatResponseDto;
import com.cesi_zen_back.cesi_zen_back.entity.HistoricEtat;
import com.cesi_zen_back.cesi_zen_back.repository.HistoricEtatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HistoricEtatServiceImplTest {

    @Mock
    private HistoricEtatRepository historicEtatRepository;

    @InjectMocks
    private HistoricEtatServiceImpl service;

    private UUID entityId;
    private HistoricEtat history;

    @BeforeEach
    void setUp() {
        entityId = UUID.randomUUID();

        history = new HistoricEtat();
        history.setId(UUID.randomUUID());
        history.setOldValue("old");
        history.setNewValue("new");
        history.setComment("UPDATE");
        history.setEntityType("RESSOURCE");
        history.setEntityId(entityId);
        history.setModificationDate(LocalDateTime.now());
    }

    @Test
    void getAllHistory_shouldMapAllEntities() {
        when(historicEtatRepository.findAll()).thenReturn(List.of(history));

        List<HistoricEtatResponseDto> result = service.getAllHistory();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).oldValue()).isEqualTo("old");
        assertThat(result.get(0).newValue()).isEqualTo("new");
    }

    @Test
    void getHistoryByEntity_shouldFilterByEntityTypeAndId() {
        HistoricEtat other = new HistoricEtat();
        other.setEntityType("APP_USER");
        other.setEntityId(UUID.randomUUID());

        when(historicEtatRepository.findAll()).thenReturn(List.of(history, other));

        List<HistoricEtatResponseDto> result = service.getHistoryByEntity("RESSOURCE", entityId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).entityType()).isEqualTo("RESSOURCE");
        assertThat(result.get(0).entityId()).isEqualTo(entityId);
    }
}