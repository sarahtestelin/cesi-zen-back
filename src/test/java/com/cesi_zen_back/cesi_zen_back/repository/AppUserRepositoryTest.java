package com.cesi_zen_back.cesi_zen_back.repository;

import com.cesi_zen_back.cesi_zen_back.entity.AppUser;
import com.cesi_zen_back.cesi_zen_back.entity.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
class AppUserRepositoryTest {

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    void findByMail_shouldReturnUser() {
        Role role = roleRepository.save(Role.builder()
                .roleName("USER")
                .build());

        AppUser user = AppUser.builder()
                .mail("user@test.fr")
                .pseudo("Sarah")
                .hashedPassword("hashed-password")
                .isActive(true)
                .lastConnexion(LocalDateTime.now())
                .role(role)
                .build();

        appUserRepository.save(user);

        assertThat(appUserRepository.findByMail("user@test.fr"))
                .isPresent()
                .get()
                .extracting(AppUser::getPseudo)
                .isEqualTo("Sarah");
    }

    @Test
    void existsByMailAndPseudo_shouldReturnTrueWhenAlreadyUsed() {
        Role role = roleRepository.save(Role.builder()
                .roleName("USER")
                .build());

        appUserRepository.save(AppUser.builder()
                .mail("existing@test.fr")
                .pseudo("ExistingPseudo")
                .hashedPassword("hashed-password")
                .isActive(true)
                .lastConnexion(LocalDateTime.now())
                .role(role)
                .build());

        assertThat(appUserRepository.existsByMail("existing@test.fr")).isTrue();
        assertThat(appUserRepository.existsByPseudo("ExistingPseudo")).isTrue();
    }

    @Test
    void findByIsActiveTrueAndLastConnexionBefore_shouldReturnInactiveCandidates() {
        Role role = roleRepository.save(Role.builder()
                .roleName("USER")
                .build());

        AppUser oldActiveUser = appUserRepository.save(AppUser.builder()
                .mail("old@test.fr")
                .pseudo("OldUser")
                .hashedPassword("hashed-password")
                .isActive(true)
                .lastConnexion(LocalDateTime.now().minusDays(100))
                .role(role)
                .build());

        appUserRepository.save(AppUser.builder()
                .mail("recent@test.fr")
                .pseudo("RecentUser")
                .hashedPassword("hashed-password")
                .isActive(true)
                .lastConnexion(LocalDateTime.now())
                .role(role)
                .build());

        appUserRepository.save(AppUser.builder()
                .mail("disabled@test.fr")
                .pseudo("DisabledUser")
                .hashedPassword("hashed-password")
                .isActive(false)
                .lastConnexion(LocalDateTime.now().minusDays(100))
                .role(role)
                .build());

        assertThat(appUserRepository.findByIsActiveTrueAndLastConnexionBefore(LocalDateTime.now().minusDays(90)))
                .extracting(AppUser::getIdUser)
                .containsExactly(oldActiveUser.getIdUser());
    }
}