package com.cesi_zen_back.cesi_zen_back.controller;

import com.cesi_zen_back.cesi_zen_back.dto.HistoricEtatResponseDto;
import com.cesi_zen_back.cesi_zen_back.service.HistoricEtatService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/history")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class HistoricEtatController {

    private final HistoricEtatService historicEtatService;

    @GetMapping
    public List<HistoricEtatResponseDto> getAllHistory() {
        return historicEtatService.getAllHistory();
    }

    @GetMapping("/{entityType}/{entityId}")
    public List<HistoricEtatResponseDto> getHistoryByEntity(
            @PathVariable String entityType,
            @PathVariable UUID entityId
    ) {
        return historicEtatService.getHistoryByEntity(entityType, entityId);
    }
}