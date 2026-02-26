package com.cesi_zen_back.cesi_zen_back.controller;

import com.cesi_zen_back.cesi_zen_back.entity.HistoricEtat;
import com.cesi_zen_back.cesi_zen_back.repository.HistoricEtatRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/historic-etats")
public class HistoricEtatController {

    private final HistoricEtatRepository repo;

    public HistoricEtatController(HistoricEtatRepository repo) {
        this.repo = repo;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HistoricEtat create(@RequestBody HistoricEtat historicEtat) {
        historicEtat.setId(null);
        return repo.save(historicEtat);
    }

    @GetMapping
    public List<HistoricEtat> list() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public HistoricEtat get(@PathVariable UUID id) {
        return repo.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "HistoricEtat not found"));
    }

    @PutMapping("/{id}")
    public HistoricEtat update(@PathVariable UUID id, @RequestBody HistoricEtat body) {
        HistoricEtat existing = get(id);
        body.setId(existing.getId());
        return repo.save(body);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        if (!repo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "HistoricEtat not found");
        }
        repo.deleteById(id);
    }
}