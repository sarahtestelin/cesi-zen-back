package com.cesi_zen_back.cesi_zen_back.service;

import com.cesi_zen_back.cesi_zen_back.dto.HistoricEtatResponseDto;
import com.cesi_zen_back.cesi_zen_back.dto.RessourceResponseDto;
import com.cesi_zen_back.cesi_zen_back.entity.HistoricEtat;
import com.cesi_zen_back.cesi_zen_back.entity.Ressource;
import com.cesi_zen_back.cesi_zen_back.repository.HistoricEtatRepository;
import com.cesi_zen_back.cesi_zen_back.repository.RessourceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RessourceServiceImplTest {

    @Mock
    private RessourceRepository repo;

    @Mock
    private HistoricEtatRepository historicRepo;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @InjectMocks
    private RessourceServiceImpl service;

    private UUID ressourceId;
    private Ressource activeRessource;

    @BeforeEach
    void setUp() {
        ressourceId = UUID.randomUUID();

        activeRessource = new Ressource();
        activeRessource.setId(ressourceId);
        activeRessource.setTitle("Gestion du stress");
        activeRessource.setDescription("Description santé mentale");
        activeRessource.setCategory("stress");
        activeRessource.setStatus("PUBLISHED");
        activeRessource.setRessourceIsActive(true);
        activeRessource.setRessourceIsUsed(true);
        activeRessource.setCreatedAt(LocalDateTime.now().minusDays(1));
        activeRessource.setUpdatedAt(LocalDateTime.now());
        activeRessource.setVersion(1);
    }

    @Test
    void getPublic_shouldReturnActiveRessource() {
        when(repo.findById(ressourceId)).thenReturn(Optional.of(activeRessource));

        RessourceResponseDto result = service.getPublic(ressourceId);

        assertThat(result.id()).isEqualTo(ressourceId);
        assertThat(result.title()).isEqualTo("Gestion du stress");
        assertThat(result.ressourceIsActive()).isTrue();
    }

    @Test
    void getPublic_shouldThrowNotFound_whenRessourceIsInactive() {
        activeRessource.setRessourceIsActive(false);

        when(repo.findById(ressourceId)).thenReturn(Optional.of(activeRessource));

        assertThatThrownBy(() -> service.getPublic(ressourceId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Ressource introuvable");
    }

    @Test
    void getAdmin_shouldReturnRessourceEvenIfInactive() {
        activeRessource.setRessourceIsActive(false);

        when(repo.findById(ressourceId)).thenReturn(Optional.of(activeRessource));

        RessourceResponseDto result = service.getAdmin(ressourceId);

        assertThat(result.id()).isEqualTo(ressourceId);
        assertThat(result.ressourceIsActive()).isFalse();
    }

    @Test
    void getAdmin_shouldThrowNotFound_whenRessourceDoesNotExist() {
        when(repo.findById(ressourceId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getAdmin(ressourceId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Ressource introuvable");
    }

    @Test
    void create_shouldForceActiveUsedAndSaveHistory() {
        Ressource body = new Ressource();
        body.setId(UUID.randomUUID());
        body.setTitle("Nouvelle ressource");
        body.setDescription("Description");
        body.setCategory("anxiete");
        body.setStatus(null);
        body.setRessourceIsActive(false);
        body.setRessourceIsUsed(false);

        when(repo.save(any(Ressource.class))).thenAnswer(invocation -> {
            Ressource saved = invocation.getArgument(0);
            saved.setId(ressourceId);
            saved.setStatus("PUBLISHED");
            saved.setVersion(0);
            return saved;
        });

        RessourceResponseDto result = service.create(body);

        assertThat(result.id()).isEqualTo(ressourceId);
        assertThat(result.ressourceIsActive()).isTrue();
        assertThat(result.ressourceIsUsed()).isTrue();
        assertThat(result.title()).isEqualTo("Nouvelle ressource");

        ArgumentCaptor<HistoricEtat> captor = ArgumentCaptor.forClass(HistoricEtat.class);
        verify(historicRepo).save(captor.capture());

        HistoricEtat history = captor.getValue();
        assertThat(history.getOldValue()).isEqualTo("{}");
        assertThat(history.getComment()).isEqualTo("CREATION");
        assertThat(history.getEntityType()).isEqualTo("RESSOURCE");
        assertThat(history.getEntityId()).isEqualTo(ressourceId);
        assertThat(history.getNewValue()).contains("Nouvelle ressource");
    }

    @Test
    void update_shouldUpdateFieldsAndSaveHistory() {
        Ressource body = new Ressource();
        body.setTitle("Titre modifié");
        body.setDescription("Description modifiée");
        body.setCategory("sommeil");
        body.setStatus("DRAFT");
        body.setRessourceIsActive(false);
        body.setRessourceIsUsed(false);

        when(repo.findById(ressourceId)).thenReturn(Optional.of(activeRessource));
        when(repo.save(activeRessource)).thenReturn(activeRessource);

        RessourceResponseDto result = service.update(ressourceId, body);

        assertThat(result.title()).isEqualTo("Titre modifié");
        assertThat(result.description()).isEqualTo("Description modifiée");
        assertThat(result.category()).isEqualTo("sommeil");
        assertThat(result.status()).isEqualTo("DRAFT");
        assertThat(result.ressourceIsActive()).isFalse();
        assertThat(result.ressourceIsUsed()).isFalse();

        ArgumentCaptor<HistoricEtat> captor = ArgumentCaptor.forClass(HistoricEtat.class);
        verify(historicRepo).save(captor.capture());

        assertThat(captor.getValue().getComment()).isEqualTo("UPDATE");
        assertThat(captor.getValue().getOldValue()).contains("Gestion du stress");
        assertThat(captor.getValue().getNewValue()).contains("Titre modifié");
    }

    @Test
    void enable_shouldActivateRessourceAndSaveHistory() {
        activeRessource.setRessourceIsActive(false);
        activeRessource.setRessourceIsUsed(false);

        when(repo.findById(ressourceId)).thenReturn(Optional.of(activeRessource));
        when(repo.save(activeRessource)).thenReturn(activeRessource);

        RessourceResponseDto result = service.enable(ressourceId);

        assertThat(result.ressourceIsActive()).isTrue();
        assertThat(result.ressourceIsUsed()).isTrue();

        ArgumentCaptor<HistoricEtat> captor = ArgumentCaptor.forClass(HistoricEtat.class);
        verify(historicRepo).save(captor.capture());

        assertThat(captor.getValue().getComment()).isEqualTo("ENABLE");
    }

    @Test
    void disable_shouldDeactivateRessourceAndSaveHistory() {
        when(repo.findById(ressourceId)).thenReturn(Optional.of(activeRessource));
        when(repo.save(activeRessource)).thenReturn(activeRessource);

        RessourceResponseDto result = service.disable(ressourceId);

        assertThat(result.ressourceIsActive()).isFalse();
        assertThat(result.ressourceIsUsed()).isFalse();

        ArgumentCaptor<HistoricEtat> captor = ArgumentCaptor.forClass(HistoricEtat.class);
        verify(historicRepo).save(captor.capture());

        assertThat(captor.getValue().getComment()).isEqualTo("DISABLE");
    }

    @Test
    void delete_shouldSoftDeleteRessourceAndSaveHistory() {
        when(repo.findById(ressourceId)).thenReturn(Optional.of(activeRessource));
        when(repo.save(activeRessource)).thenReturn(activeRessource);

        service.delete(ressourceId);

        assertThat(activeRessource.isRessourceIsActive()).isFalse();
        assertThat(activeRessource.isRessourceIsUsed()).isFalse();

        ArgumentCaptor<HistoricEtat> captor = ArgumentCaptor.forClass(HistoricEtat.class);
        verify(historicRepo).save(captor.capture());

        assertThat(captor.getValue().getComment()).isEqualTo("DELETE_LOGIQUE");
    }

    @Test
    void history_shouldReturnOnlyHistoryForRequestedRessource() {
        HistoricEtat matchingHistory = new HistoricEtat();
        matchingHistory.setId(UUID.randomUUID());
        matchingHistory.setEntityType("RESSOURCE");
        matchingHistory.setEntityId(ressourceId);
        matchingHistory.setOldValue("{}");
        matchingHistory.setNewValue("{title}");
        matchingHistory.setComment("UPDATE");
        matchingHistory.setModificationDate(LocalDateTime.now());

        HistoricEtat otherRessourceHistory = new HistoricEtat();
        otherRessourceHistory.setEntityType("RESSOURCE");
        otherRessourceHistory.setEntityId(UUID.randomUUID());

        HistoricEtat otherEntityHistory = new HistoricEtat();
        otherEntityHistory.setEntityType("APP_USER");
        otherEntityHistory.setEntityId(ressourceId);

        when(historicRepo.findAll()).thenReturn(List.of(
                matchingHistory,
                otherRessourceHistory,
                otherEntityHistory
        ));

        List<HistoricEtatResponseDto> result = service.history(ressourceId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).entityType()).isEqualTo("RESSOURCE");
        assertThat(result.get(0).entityId()).isEqualTo(ressourceId);
    }

    @Test
    void update_shouldThrowNotFound_whenRessourceDoesNotExist() {
        when(repo.findById(ressourceId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(ressourceId, new Ressource()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Ressource introuvable");

        verify(repo, never()).save(any());
        verify(historicRepo, never()).save(any());
    }
}