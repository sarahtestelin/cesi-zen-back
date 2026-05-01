package com.cesi_zen_back.cesi_zen_back.controller;

import com.cesi_zen_back.cesi_zen_back.dto.HistoricEtatResponseDto;
import com.cesi_zen_back.cesi_zen_back.dto.RessourceResponseDto;
import com.cesi_zen_back.cesi_zen_back.entity.Ressource;
import com.cesi_zen_back.cesi_zen_back.service.RessourceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class RessourceControllerFunctionalTest {

    private MockMvc mockMvc;
    private RessourceService ressourceService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private UUID ressourceId;

    @BeforeEach
    void setUp() {
        ressourceService = mock(RessourceService.class);
        ressourceId = UUID.randomUUID();

        MappingJackson2HttpMessageConverter jacksonConverter =
                new MappingJackson2HttpMessageConverter(objectMapper);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new RessourceController(ressourceService))
                .setMessageConverters(jacksonConverter)
                .build();
    }

    @Test
    void listPublic_shouldReturnPublicResourcesAndForwardFilters() throws Exception {
        RessourceResponseDto dto = new RessourceResponseDto(
                ressourceId,
                true,
                true,
                "Gestion du stress",
                "Description",
                "PUBLISHED",
                "stress",
                LocalDateTime.now(),
                LocalDateTime.now(),
                1
        );

        when(ressourceService.listPublic("stress", "sante"))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/ressources")
                        .param("search", "stress")
                        .param("category", "sante"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(ressourceId.toString()))
                .andExpect(jsonPath("$[0].title").value("Gestion du stress"))
                .andExpect(jsonPath("$[0].ressourceIsActive").value(true));

        verify(ressourceService).listPublic("stress", "sante");
    }

    @Test
    void getPublic_shouldReturnOneResource() throws Exception {
        RessourceResponseDto dto = new RessourceResponseDto(
                ressourceId,
                true,
                true,
                "Titre",
                "Description",
                "PUBLISHED",
                "stress",
                LocalDateTime.now(),
                LocalDateTime.now(),
                1
        );

        when(ressourceService.getPublic(ressourceId)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/ressources/{id}", ressourceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ressourceId.toString()))
                .andExpect(jsonPath("$.title").value("Titre"));

        verify(ressourceService).getPublic(ressourceId);
    }

    @Test
    void listAdmin_shouldReturnAdminResourcesAndForwardFilters() throws Exception {
        RessourceResponseDto dto = new RessourceResponseDto(
                ressourceId,
                false,
                false,
                "Archive",
                "Description",
                "DRAFT",
                "stress",
                LocalDateTime.now(),
                LocalDateTime.now(),
                2
        );

        when(ressourceService.listAdmin("archive", "stress", false))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/ressources/admin")
                        .param("search", "archive")
                        .param("category", "stress")
                        .param("active", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Archive"))
                .andExpect(jsonPath("$[0].ressourceIsActive").value(false));

        verify(ressourceService).listAdmin("archive", "stress", false);
    }

    @Test
    void create_shouldReturnCreatedAndCallService() throws Exception {
        RessourceResponseDto response = new RessourceResponseDto(
                ressourceId,
                true,
                true,
                "Nouvelle ressource",
                "Description",
                "PUBLISHED",
                "stress",
                LocalDateTime.now(),
                LocalDateTime.now(),
                1
        );

        when(ressourceService.create(any(Ressource.class))).thenReturn(response);

        String body = """
                {
                  "title": "Nouvelle ressource",
                  "description": "Description",
                  "category": "stress",
                  "status": "PUBLISHED",
                  "ressourceIsActive": true,
                  "ressourceIsUsed": true
                }
                """;

        mockMvc.perform(post("/api/v1/ressources")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(ressourceId.toString()))
                .andExpect(jsonPath("$.title").value("Nouvelle ressource"));

        ArgumentCaptor<Ressource> captor = ArgumentCaptor.forClass(Ressource.class);
        verify(ressourceService).create(captor.capture());

        assertThat(captor.getValue().getTitle()).isEqualTo("Nouvelle ressource");
        assertThat(captor.getValue().getCategory()).isEqualTo("stress");
    }

    @Test
    void update_shouldReturnUpdatedResourceAndCallService() throws Exception {
        RessourceResponseDto response = new RessourceResponseDto(
                ressourceId,
                true,
                true,
                "Ressource modifiée",
                "Description modifiée",
                "PUBLISHED",
                "stress",
                LocalDateTime.now(),
                LocalDateTime.now(),
                2
        );

        when(ressourceService.update(eq(ressourceId), any(Ressource.class))).thenReturn(response);

        String body = """
                {
                  "title": "Ressource modifiée",
                  "description": "Description modifiée",
                  "category": "stress"
                }
                """;

        mockMvc.perform(put("/api/v1/ressources/{id}", ressourceId)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Ressource modifiée"))
                .andExpect(jsonPath("$.version").value(2));

        verify(ressourceService).update(eq(ressourceId), any(Ressource.class));
    }

    @Test
    void disable_shouldReturnDisabledResource() throws Exception {
        RessourceResponseDto response = new RessourceResponseDto(
                ressourceId,
                false,
                false,
                "Ressource",
                "Description",
                "PUBLISHED",
                "stress",
                LocalDateTime.now(),
                LocalDateTime.now(),
                1
        );

        when(ressourceService.disable(ressourceId)).thenReturn(response);

        mockMvc.perform(patch("/api/v1/ressources/{id}/disable", ressourceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ressourceIsActive").value(false))
                .andExpect(jsonPath("$.ressourceIsUsed").value(false));

        verify(ressourceService).disable(ressourceId);
    }

    @Test
    void enable_shouldReturnEnabledResource() throws Exception {
        RessourceResponseDto response = new RessourceResponseDto(
                ressourceId,
                true,
                true,
                "Ressource",
                "Description",
                "PUBLISHED",
                "stress",
                LocalDateTime.now(),
                LocalDateTime.now(),
                1
        );

        when(ressourceService.enable(ressourceId)).thenReturn(response);

        mockMvc.perform(patch("/api/v1/ressources/{id}/enable", ressourceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ressourceIsActive").value(true))
                .andExpect(jsonPath("$.ressourceIsUsed").value(true));

        verify(ressourceService).enable(ressourceId);
    }

    @Test
    void delete_shouldReturnNoContentAndCallService() throws Exception {
        mockMvc.perform(delete("/api/v1/ressources/{id}", ressourceId))
                .andExpect(status().isNoContent());

        verify(ressourceService).delete(ressourceId);
    }

    @Test
    void history_shouldReturnResourceHistory() throws Exception {
        UUID historyId = UUID.randomUUID();

        HistoricEtatResponseDto history = new HistoricEtatResponseDto(
                historyId,
                "{}",
                "{title}",
                "UPDATE",
                "RESSOURCE",
                ressourceId,
                LocalDateTime.now()
        );

        when(ressourceService.history(ressourceId)).thenReturn(List.of(history));

        mockMvc.perform(get("/api/v1/ressources/{id}/history", ressourceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(historyId.toString()))
                .andExpect(jsonPath("$[0].comment").value("UPDATE"))
                .andExpect(jsonPath("$[0].entityType").value("RESSOURCE"));

        verify(ressourceService).history(ressourceId);
    }
}