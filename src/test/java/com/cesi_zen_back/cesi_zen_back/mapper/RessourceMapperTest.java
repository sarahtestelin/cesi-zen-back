package com.cesi_zen_back.cesi_zen_back.mapper;

import com.cesi_zen_back.cesi_zen_back.dto.RessourceResponseDto;
import com.cesi_zen_back.cesi_zen_back.entity.Ressource;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RessourceMapperTest {

    @Test
    void toDto_shouldMapRessourceFields() {
        UUID id = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        LocalDateTime updatedAt = LocalDateTime.now();

        Ressource ressource = new Ressource();
        ressource.setId(id);
        ressource.setRessourceIsActive(true);
        ressource.setRessourceIsUsed(true);
        ressource.setTitle("Titre");
        ressource.setDescription("Description");
        ressource.setStatus("PUBLISHED");
        ressource.setCategory("stress");
        ressource.setCreatedAt(createdAt);
        ressource.setUpdatedAt(updatedAt);
        ressource.setVersion(2);

        RessourceResponseDto dto = RessourceMapper.toDto(ressource);

        assertThat(dto.id()).isEqualTo(id);
        assertThat(dto.ressourceIsActive()).isTrue();
        assertThat(dto.ressourceIsUsed()).isTrue();
        assertThat(dto.title()).isEqualTo("Titre");
        assertThat(dto.description()).isEqualTo("Description");
        assertThat(dto.status()).isEqualTo("PUBLISHED");
        assertThat(dto.category()).isEqualTo("stress");
        assertThat(dto.version()).isEqualTo(2);
    }
}