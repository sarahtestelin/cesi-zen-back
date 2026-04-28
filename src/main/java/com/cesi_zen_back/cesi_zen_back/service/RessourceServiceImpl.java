package com.cesi_zen_back.cesi_zen_back.service;

import com.cesi_zen_back.cesi_zen_back.dto.HistoricEtatResponseDto;
import com.cesi_zen_back.cesi_zen_back.dto.RessourceResponseDto;
import com.cesi_zen_back.cesi_zen_back.entity.HistoricEtat;
import com.cesi_zen_back.cesi_zen_back.entity.Ressource;
import com.cesi_zen_back.cesi_zen_back.mapper.HistoricEtatMapper;
import com.cesi_zen_back.cesi_zen_back.mapper.RessourceMapper;
import com.cesi_zen_back.cesi_zen_back.repository.HistoricEtatRepository;
import com.cesi_zen_back.cesi_zen_back.repository.RessourceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RessourceServiceImpl implements RessourceService {

    private final RessourceRepository repo;
    private final HistoricEtatRepository historicRepo;
    private final ObjectMapper objectMapper;

    @Override
    public List<RessourceResponseDto> listPublic(String search, String category) {
        return repo.findAll(buildSpec(search, category, true))
                .stream()
                .map(RessourceMapper::toDto)
                .toList();
    }

    @Override
    public RessourceResponseDto getPublic(UUID id) {
        Ressource ressource = getEntity(id);

        if (!ressource.isRessourceIsActive()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ressource introuvable");
        }

        return RessourceMapper.toDto(ressource);
    }

    @Override
    public List<RessourceResponseDto> listAdmin(String search, String category, Boolean active) {
        return repo.findAll(buildSpec(search, category, active))
                .stream()
                .map(RessourceMapper::toDto)
                .toList();
    }

    @Override
    public RessourceResponseDto getAdmin(UUID id) {
        return RessourceMapper.toDto(getEntity(id));
    }

    @Override
    public RessourceResponseDto create(Ressource ressource) {
        ressource.setId(null);
        ressource.setRessourceIsActive(true);
        ressource.setRessourceIsUsed(true);
        ressource.setCreatedAt(LocalDateTime.now());

        Ressource saved = repo.save(ressource);
        saveHistory(null, saved, "CREATION");

        return RessourceMapper.toDto(saved);
    }

    @Override
    public RessourceResponseDto update(UUID id, Ressource body) {
        Ressource existing = getEntity(id);
        String oldValue = toJson(existing);

        existing.setTitle(body.getTitle());
        existing.setDescription(body.getDescription());
        existing.setStatus(body.getStatus());
        existing.setCategory(body.getCategory());
        existing.setRessourceIsActive(body.isRessourceIsActive());
        existing.setRessourceIsUsed(body.isRessourceIsUsed());

        Ressource saved = repo.save(existing);
        saveHistory(oldValue, saved, "UPDATE");

        return RessourceMapper.toDto(saved);
    }

    @Override
    public RessourceResponseDto enable(UUID id) {
        Ressource existing = getEntity(id);
        String oldValue = toJson(existing);

        existing.setRessourceIsActive(true);
        existing.setRessourceIsUsed(true);

        Ressource saved = repo.save(existing);
        saveHistory(oldValue, saved, "ENABLE");

        return RessourceMapper.toDto(saved);
    }

    @Override
    public RessourceResponseDto disable(UUID id) {
        Ressource existing = getEntity(id);
        String oldValue = toJson(existing);

        existing.setRessourceIsActive(false);
        existing.setRessourceIsUsed(false);

        Ressource saved = repo.save(existing);
        saveHistory(oldValue, saved, "DISABLE");

        return RessourceMapper.toDto(saved);
    }

    @Override
    public void delete(UUID id) {
        Ressource existing = getEntity(id);
        String oldValue = toJson(existing);

        existing.setRessourceIsActive(false);
        existing.setRessourceIsUsed(false);

        Ressource saved = repo.save(existing);
        saveHistory(oldValue, saved, "DELETE_LOGIQUE");
    }

    @Override
    public List<HistoricEtatResponseDto> history(UUID id) {
        return historicRepo.findAll().stream()
                .filter(h -> "RESSOURCE".equals(h.getEntityType()))
                .filter(h -> id.equals(h.getEntityId()))
                .map(HistoricEtatMapper::toDto)
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