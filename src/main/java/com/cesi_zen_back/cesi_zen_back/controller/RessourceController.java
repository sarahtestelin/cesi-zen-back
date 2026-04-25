package com.cesi_zen_back.cesi_zen_back.controller;

import com.cesi_zen_back.cesi_zen_back.entity.Ressource;
import com.cesi_zen_back.cesi_zen_back.repository.RessourceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ressources")
public class RessourceController {

    private final RessourceRepository repo;

    public RessourceController(RessourceRepository repo) {
        this.repo = repo;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Ressource create(@RequestBody Ressource r) {
        r.setId(null);
        return repo.save(r);
    }

    @GetMapping
    public List<Ressource> list() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public Ressource get(@PathVariable UUID id) {
        return repo.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Ressource not found"));
    }

    @PutMapping("/{id}")
    public Ressource update(@PathVariable UUID id, @RequestBody Ressource body) {
        Ressource existing = get(id);
        body.setId(existing.getId());
        return repo.save(body);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        if (!repo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ressource not found");
        }
        repo.deleteById(id);
    }
}