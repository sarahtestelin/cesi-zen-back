package com.cesi_zen_back.cesi_zen_back.controller;

import com.cesi_zen_back.cesi_zen_back.dto.HistoricEtatResponseDto;
import com.cesi_zen_back.cesi_zen_back.service.HistoricEtatService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class HistoricEtatControllerFunctionalTest {

    private MockMvc mockMvc;
    private HistoricEtatService historicEtatService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @BeforeEach
    void setUp() {
        historicEtatService = mock(HistoricEtatService.class);

        MappingJackson2HttpMessageConverter jacksonConverter =
                new MappingJackson2HttpMessageConverter(objectMapper);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new HistoricEtatController(historicEtatService))
                .setMessageConverters(jacksonConverter)
                .build();
    }

    @Test
    void getAllHistory_shouldReturnHistoryList() throws Exception {
        UUID historyId = UUID.randomUUID();
        UUID entityId = UUID.randomUUID();

        HistoricEtatResponseDto history = new HistoricEtatResponseDto(
                historyId,
                "{}",
                "{new}",
                "UPDATE",
                "RESSOURCE",
                entityId,
                LocalDateTime.now()
        );

        when(historicEtatService.getAllHistory()).thenReturn(List.of(history));

        mockMvc.perform(get("/api/admin/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(historyId.toString()))
                .andExpect(jsonPath("$[0].comment").value("UPDATE"))
                .andExpect(jsonPath("$[0].entityType").value("RESSOURCE"));

        verify(historicEtatService).getAllHistory();
    }

    @Test
    void getHistoryByEntity_shouldForwardEntityTypeAndId() throws Exception {
        UUID historyId = UUID.randomUUID();
        UUID entityId = UUID.randomUUID();

        HistoricEtatResponseDto history = new HistoricEtatResponseDto(
                historyId,
                "old",
                "new",
                "DISABLE",
                "APP_USER",
                entityId,
                LocalDateTime.now()
        );

        when(historicEtatService.getHistoryByEntity("APP_USER", entityId))
                .thenReturn(List.of(history));

        mockMvc.perform(get("/api/admin/history/{entityType}/{entityId}", "APP_USER", entityId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(historyId.toString()))
                .andExpect(jsonPath("$[0].comment").value("DISABLE"))
                .andExpect(jsonPath("$[0].entityId").value(entityId.toString()));

        verify(historicEtatService).getHistoryByEntity("APP_USER", entityId);
    }
}