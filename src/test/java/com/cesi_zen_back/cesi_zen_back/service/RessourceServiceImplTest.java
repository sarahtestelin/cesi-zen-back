package com.cesi_zen_back.cesi_zen_back.service;

import com.cesi_zen_back.cesi_zen_back.dto.RessourceRequestDto;
import com.cesi_zen_back.cesi_zen_back.dto.RessourceResponseDto;
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

    @Mock
    private ObjectMapper objectMapper;

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
    void create_shouldSaveRessourceAndHistory() throws Exception {
        RessourceRequestDto dto = new RessourceRequestDto("Nouvelle ressource", "Description", "anxiete");

        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(repo.save(any(Ressource.class))).thenAnswer(invocation -> {
            Ressource saved = invocation.getArgument(0);
            saved.setId(ressourceId);
            saved.setStatus("PUBLISHED");
            saved.setVersion(0);
            return saved;
        });

        RessourceResponseDto result = service.create(dto);

        assertThat(result.id()).isEqualTo(ressourceId);
        assertThat(result.title()).isEqualTo("Nouvelle ressource");
        assertThat(result.ressourceIsActive()).isTrue();

        verify(historicRepo).save(any());
    }

    @Test
    void update_shouldUpdateFieldsAndSaveHistory() throws Exception {
        RessourceRequestDto dto = new RessourceRequestDto("Titre modifié", "Description modifiée", "sommeil");

        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(repo.findById(ressourceId)).thenReturn(Optional.of(activeRessource));
        when(repo.save(activeRessource)).thenReturn(activeRessource);

        RessourceResponseDto result = service.update(ressourceId, dto);

        assertThat(result.title()).isEqualTo("Titre modifié");
        assertThat(result.description()).isEqualTo("Description modifiée");
        assertThat(result.category()).isEqualTo("sommeil");

        verify(historicRepo).save(any());
    }

    @Test
    void enable_shouldActivateRessource() throws Exception {
        activeRessource.setRessourceIsActive(false);
        activeRessource.setRessourceIsUsed(false);

        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(repo.findById(ressourceId)).thenReturn(Optional.of(activeRessource));
        when(repo.save(activeRessource)).thenReturn(activeRessource);

        RessourceResponseDto result = service.enable(ressourceId);

        assertThat(result.ressourceIsActive()).isTrue();
        assertThat(result.ressourceIsUsed()).isTrue();
    }

    @Test
    void disable_shouldDeactivateRessource() throws Exception {
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(repo.findById(ressourceId)).thenReturn(Optional.of(activeRessource));
        when(repo.save(activeRessource)).thenReturn(activeRessource);

        RessourceResponseDto result = service.disable(ressourceId);

        assertThat(result.ressourceIsActive()).isFalse();
        assertThat(result.ressourceIsUsed()).isFalse();
    }

    @Test
    void delete_shouldSoftDeleteRessource() throws Exception {
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(repo.findById(ressourceId)).thenReturn(Optional.of(activeRessource));
        when(repo.save(activeRessource)).thenReturn(activeRessource);

        service.delete(ressourceId);

        assertThat(activeRessource.isRessourceIsActive()).isFalse();
        assertThat(activeRessource.isRessourceIsUsed()).isFalse();
        verify(historicRepo).save(any());
    }

    @Test
    void update_shouldThrowNotFound_whenRessourceDoesNotExist() {
        when(repo.findById(ressourceId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(ressourceId, new RessourceRequestDto("t", "d", "c")))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Ressource introuvable");

        verify(repo, never()).save(any());
    }
}
