package com.cesi_zen_back.cesi_zen_back.mapper;

import com.cesi_zen_back.cesi_zen_back.dto.AppUserDto;
import com.cesi_zen_back.cesi_zen_back.entity.AppUser;
import com.cesi_zen_back.cesi_zen_back.entity.Role;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    @Test
    void toDto_shouldMapUserFields() {
        UUID id = UUID.randomUUID();
        LocalDateTime lastConnexion = LocalDateTime.now();

        AppUser user = AppUser.builder()
                .idUser(id)
                .mail("user@test.fr")
                .pseudo("Sarah")
                .isActive(true)
                .lastConnexion(lastConnexion)
                .role(Role.builder().roleName("USER").build())
                .build();

        AppUserDto dto = UserMapper.toDto(user);

        assertThat(dto.id()).isEqualTo(id);
        assertThat(dto.mail()).isEqualTo("user@test.fr");
        assertThat(dto.pseudo()).isEqualTo("Sarah");
        assertThat(dto.appUserIsActive()).isTrue();
        assertThat(dto.lastConnectionAt()).isEqualTo(lastConnexion);
        assertThat(dto.role()).isEqualTo("USER");
    }
}