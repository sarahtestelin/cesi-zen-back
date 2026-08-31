package com.cesi_zen_back.cesi_zen_back.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cesi_zen_back.cesi_zen_back.entity.Ressource;
import com.cesi_zen_back.cesi_zen_back.repository.RessourceRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RessourceIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private RessourceRepository ressourceRepository;

  @BeforeEach
  void setUp() {
    ressourceRepository.deleteAll();

    Ressource ressource = new Ressource();
    ressource.setTitle("Gestion du stress");
    ressource.setDescription("Ressource de test pour gérer le stress");
    ressource.setCategory("BIEN_ETRE");
    ressource.setStatus("PUBLISHED");
    ressource.setRessourceIsActive(true);
    ressource.setRessourceIsUsed(true);
    ressource.setCreatedAt(LocalDateTime.now());

    ressourceRepository.save(ressource);
  }

  @Test
  void shouldReturnActiveResourcesFromDatabase() throws Exception {
    mockMvc
        .perform(get("/api/v1/ressources"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].title").value("Gestion du stress"))
        .andExpect(jsonPath("$[0].category").value("BIEN_ETRE"));
  }

  @Test
  void shouldFilterResourcesBySearch() throws Exception {
    mockMvc
        .perform(get("/api/v1/ressources").param("search", "stress"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].title").value("Gestion du stress"));
  }

  @Test
  void shouldReturnEmptyListWhenCategoryDoesNotMatch() throws Exception {
    mockMvc
        .perform(get("/api/v1/ressources").param("category", "INEXISTANTE"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));
  }
}
