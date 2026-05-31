package com.cesi_zen_back.cesi_zen_back.controller;

import com.cesi_zen_back.cesi_zen_back.dto.RessourceRequestDto;
import com.cesi_zen_back.cesi_zen_back.dto.RessourceResponseDto;
import com.cesi_zen_back.cesi_zen_back.service.RessourceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class RessourceControllerTest {

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
    void listPublic_shouldReturnPublicResources() throws Exception {
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
                .andExpect(jsonPath("$.title").value("Titre"));

        verify(ressourceService).getPublic(ressourceId);
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

        when(ressourceService.create(any(RessourceRequestDto.class))).thenReturn(response);

        String body = """
                {
                  "title": "Nouvelle ressource",
                  "description": "Description",
                  "category": "stress"
                }
                """;

        mockMvc.perform(post("/api/v1/ressources")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Nouvelle ressource"));

        verify(ressourceService).create(any(RessourceRequestDto.class));
    }

    @Test
    void update_shouldReturnUpdatedResource() throws Exception {
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

        when(ressourceService.update(eq(ressourceId), any(RessourceRequestDto.class))).thenReturn(response);

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
                .andExpect(jsonPath("$.title").value("Ressource modifiée"));

        verify(ressourceService).update(eq(ressourceId), any(RessourceRequestDto.class));
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
                .andExpect(jsonPath("$.ressourceIsActive").value(false));

        verify(ressourceService).disable(ressourceId);
    }

    @Test
    void delete_shouldReturnNoContentAndCallService() throws Exception {
        mockMvc.perform(delete("/api/v1/ressources/{id}", ressourceId))
                .andExpect(status().isNoContent());

        verify(ressourceService).delete(ressourceId);
    }
}
