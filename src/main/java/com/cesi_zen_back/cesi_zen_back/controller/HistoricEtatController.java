package com.cesi_zen_back.cesi_zen_back.controller;

import com.cesi_zen_back.cesi_zen_back.entity.HistoricEtat;
import com.cesi_zen_back.cesi_zen_back.repository.HistoricEtatRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/historic-etats")
@PreAuthorize("hasRole('ADMIN')")
public class HistoricEtatController {

    private final HistoricEtatRepository repo;

    public HistoricEtatController(HistoricEtatRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<HistoricEtat> list() {
        return repo.findAll();
    }
}