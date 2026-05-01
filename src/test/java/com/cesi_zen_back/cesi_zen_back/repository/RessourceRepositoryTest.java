package com.cesi_zen_back.cesi_zen_back.repository;

import com.cesi_zen_back.cesi_zen_back.entity.Ressource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
class RessourceRepositoryTest {

    @Autowired
    private RessourceRepository ressourceRepository;

    @Test
    void findAllWithSpecification_shouldFilterActiveRessources() {
        Ressource active = new Ressource();
        active.setTitle("Ressource active");
        active.setDescription("Description active");
        active.setCategory("stress");
        active.setStatus("PUBLISHED");
        active.setRessourceIsActive(true);
        active.setRessourceIsUsed(true);
        active.setCreatedAt(LocalDateTime.of(2026, 1, 1, 10, 0));

        Ressource inactive = new Ressource();
        inactive.setTitle("Ressource inactive");
        inactive.setDescription("Description inactive");
        inactive.setCategory("stress");
        inactive.setStatus("PUBLISHED");
        inactive.setRessourceIsActive(false);
        inactive.setRessourceIsUsed(false);
        inactive.setCreatedAt(LocalDateTime.of(2026, 1, 1, 11, 0));

        ressourceRepository.save(active);
        ressourceRepository.save(inactive);

        Specification<Ressource> onlyActive = (root, query, criteriaBuilder) ->
                criteriaBuilder.isTrue(root.get("ressourceIsActive"));

        assertThat(ressourceRepository.findAll(onlyActive))
                .extracting(Ressource::getTitle)
                .containsExactly("Ressource active");
    }
}