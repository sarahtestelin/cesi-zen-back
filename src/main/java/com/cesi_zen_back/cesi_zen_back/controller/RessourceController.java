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

    public RessourceController(
            RessourceRepository repo,
            HistoricEtatRepository historicRepo,
            ObjectMapper objectMapper
    ) {
        this.repo = repo;
        this.historicRepo = historicRepo;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public List<Ressource> listPublic(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category
    ) {
        return repo.findAll(buildSpec(search, category, true));
    }

    @GetMapping("/{id}")
    public Ressource getPublic(@PathVariable UUID id) {
        Ressource ressource = getEntity(id);

        if (!ressource.isRessourceIsActive()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ressource introuvable");
        }

        return ressource;
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Ressource> listAdmin(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean active
    ) {
        return repo.findAll(buildSpec(search, category, active));
    }

    @GetMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Ressource getAdmin(@PathVariable UUID id) {
        return getEntity(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public Ressource create(@RequestBody Ressource ressource) {
        ressource.setId(null);
        ressource.setRessourceIsActive(true);
        ressource.setRessourceIsUsed(true);
        ressource.setCreatedAt(LocalDateTime.now());

        Ressource saved = repo.save(ressource);
        saveHistory(null, saved, "CREATION");

        return saved;
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Ressource update(@PathVariable UUID id, @RequestBody Ressource body) {
        Ressource existing = getEntity(id);
        String oldValue = toJson(existing);

        existing.setTitle(body.getTitle());
        existing.setDescription(body.getDescription());
        existing.setCategory(body.getCategory());
        existing.setRessourceIsActive(body.isRessourceIsActive());
        existing.setRessourceIsUsed(body.isRessourceIsUsed());

        Ressource saved = repo.save(existing);
        saveHistory(oldValue, saved, "UPDATE");

        return saved;
    }

    @PatchMapping("/{id}/enable")
    @PreAuthorize("hasRole('ADMIN')")
    public Ressource enable(@PathVariable UUID id) {
        Ressource existing = getEntity(id);
        String oldValue = toJson(existing);

        existing.setRessourceIsActive(true);
        existing.setRessourceIsUsed(true);

        Ressource saved = repo.save(existing);
        saveHistory(oldValue, saved, "ENABLE");

        return saved;
    }

    @PatchMapping("/{id}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    public Ressource disable(@PathVariable UUID id) {
        Ressource existing = getEntity(id);
        String oldValue = toJson(existing);

        existing.setRessourceIsActive(false);
        existing.setRessourceIsUsed(false);

        Ressource saved = repo.save(existing);
        saveHistory(oldValue, saved, "DISABLE");

        return saved;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable UUID id) {
        Ressource existing = getEntity(id);
        String oldValue = toJson(existing);

        existing.setRessourceIsActive(false);
        existing.setRessourceIsUsed(false);

        Ressource saved = repo.save(existing);
        saveHistory(oldValue, saved, "DELETE_LOGIQUE");
    }

    @GetMapping("/{id}/history")
    @PreAuthorize("hasRole('ADMIN')")
    public List<HistoricEtat> history(@PathVariable UUID id) {
        return historicRepo.findAll().stream()
                .filter(h -> "RESSOURCE".equals(h.getEntityType()))
                .filter(h -> id.equals(h.getEntityId()))
                .toList();
    }

    private Ressource getEntity(UUID id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ressource introuvable"));
    }

    private Specification<Ressource> buildSpec(String search, String category, Boolean active) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (active != null) {
                predicates.add(cb.equal(root.get("ressourceIsActive"), active));
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

            query.orderBy(cb.desc(root.get("createdAt")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void saveHistory(String oldValue, Ressource newValue, String comment) {
        try {
            HistoricEtat historicEtat = new HistoricEtat();

            historicEtat.setOldValue(oldValue == null ? "{}" : oldValue);
            historicEtat.setNewValue(objectMapper.writeValueAsString(newValue));
            historicEtat.setComment(comment);
            historicEtat.setEntityType("RESSOURCE");
            historicEtat.setEntityId(newValue.getId());
            historicEtat.setModificationDate(LocalDateTime.now());

            historicRepo.save(historicEtat);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la sauvegarde de l'historique", e);
        }
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return "{}";
        }
    }
}