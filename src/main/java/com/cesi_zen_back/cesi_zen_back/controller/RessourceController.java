package com.cesi_zen_back.cesi_zen_back.controller;

import com.cesi_zen_back.cesi_zen_back.entity.HistoricEtat;
import com.cesi_zen_back.cesi_zen_back.entity.Ressource;
import com.cesi_zen_back.cesi_zen_back.repository.HistoricEtatRepository;
import com.cesi_zen_back.cesi_zen_back.repository.RessourceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/v1/ressources")
public class RessourceController {

    private final RessourceRepository repo;
    private final HistoricEtatRepository historicRepo;
    private final ObjectMapper objectMapper;

    public RessourceController(RessourceRepository repo,
                               HistoricEtatRepository historicRepo,
                               ObjectMapper objectMapper) {
        this.repo = repo;
        this.historicRepo = historicRepo;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public List<Ressource> listPublished(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category
    ) {
        return repo.findAll(buildSpec(search, category, "PUBLISHED"));
    }

    @GetMapping("/{id}")
    public Ressource getPublished(@PathVariable UUID id) {
        Ressource r = getEntity(id);

        if (!"PUBLISHED".equalsIgnoreCase(r.getStatus())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        return r;
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Ressource> listAdmin(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status
    ) {
        return repo.findAll(buildSpec(search, category, status));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public Ressource create(@RequestBody Ressource r) {

        r.setId(null);
        r.setCreatedAt(LocalDateTime.now());

        Ressource saved = repo.save(r);

        saveHistory(null, saved, "CREATION");

        return saved;
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Ressource update(@PathVariable UUID id, @RequestBody Ressource body) {

        Ressource existing = getEntity(id);
        String oldValue = toJson(existing);

        body.setId(existing.getId());

        Ressource saved = repo.save(body);

        saveHistory(oldValue, saved, "UPDATE");

        return saved;
    }

    @PatchMapping("/{id}/publish")
    @PreAuthorize("hasRole('ADMIN')")
    public Ressource publish(@PathVariable UUID id) {
        return updateStatus(id, "PUBLISHED");
    }

    @PatchMapping("/{id}/draft")
    @PreAuthorize("hasRole('ADMIN')")
    public Ressource draft(@PathVariable UUID id) {
        return updateStatus(id, "DRAFT");
    }

    @PatchMapping("/{id}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    public Ressource disable(@PathVariable UUID id) {
        return updateStatus(id, "DISABLED");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable UUID id) {
        updateStatus(id, "DISABLED");
    }

    @GetMapping("/{id}/history")
    @PreAuthorize("hasRole('ADMIN')")
    public List<HistoricEtat> history(@PathVariable UUID id) {
        return historicRepo.findAll().stream()
                .filter(h -> id.equals(h.getEntityId()))
                .toList();
    }

    private Ressource updateStatus(UUID id, String status) {

        Ressource existing = getEntity(id);
        String oldValue = toJson(existing);

        existing.setStatus(status);

        Ressource saved = repo.save(existing);

        saveHistory(oldValue, saved, "STATUS_" + status);

        return saved;
    }

    private Ressource getEntity(UUID id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private Specification<Ressource> buildSpec(String search, String category, String status) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (category != null && !category.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("category")), category.toLowerCase()));
            }

            if (search != null && !search.isBlank()) {

                String pattern = "%" + search.toLowerCase() + "%";

                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void saveHistory(String oldValue, Ressource newValue, String comment) {

        try {
            HistoricEtat h = new HistoricEtat();

            h.setOldValue(oldValue == null ? "{}" : oldValue);
            h.setNewValue(objectMapper.writeValueAsString(newValue));
            h.setComment(comment);
            h.setEntityType("RESSOURCE");
            h.setEntityId(newValue.getId());
            h.setModificationDate(LocalDateTime.now());

            historicRepo.save(h);

        } catch (Exception e) {
            throw new RuntimeException("Erreur historique", e);
        }
    }

    private String toJson(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (Exception e) {
            return "{}";
        }
    }
}