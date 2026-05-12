package com.cesi_zen_back.cesi_zen_back.controller;

import com.cesi_zen_back.cesi_zen_back.dto.HistoricEtatResponseDto;
import com.cesi_zen_back.cesi_zen_back.dto.RessourceRequestDto;
import com.cesi_zen_back.cesi_zen_back.dto.RessourceResponseDto;
import com.cesi_zen_back.cesi_zen_back.service.RessourceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ressources")
@RequiredArgsConstructor
public class RessourceController {

    private final RessourceService ressourceService;

    @GetMapping
    public List<RessourceResponseDto> listPublic(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category
    ) {
        return ressourceService.listPublic(search, category);
    }

    @GetMapping("/{id}")
    public RessourceResponseDto getPublic(@PathVariable UUID id) {
        return ressourceService.getPublic(id);
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public List<RessourceResponseDto> listAdmin(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean active
    ) {
        return ressourceService.listAdmin(search, category, active);
    }

    @GetMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public RessourceResponseDto getAdmin(@PathVariable UUID id) {
        return ressourceService.getAdmin(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public RessourceResponseDto create(@Valid @RequestBody RessourceRequestDto dto) {
        return ressourceService.create(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public RessourceResponseDto update(@PathVariable UUID id, @Valid @RequestBody RessourceRequestDto dto) {
        return ressourceService.update(id, dto);
    }

    @PatchMapping("/{id}/enable")
    @PreAuthorize("hasRole('ADMIN')")
    public RessourceResponseDto enable(@PathVariable UUID id) {
        return ressourceService.enable(id);
    }

    @PatchMapping("/{id}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    public RessourceResponseDto disable(@PathVariable UUID id) {
        return ressourceService.disable(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable UUID id) {
        ressourceService.delete(id);
    }

    @GetMapping("/{id}/history")
    @PreAuthorize("hasRole('ADMIN')")
    public List<HistoricEtatResponseDto> history(@PathVariable UUID id) {
        return ressourceService.history(id);
    }
}