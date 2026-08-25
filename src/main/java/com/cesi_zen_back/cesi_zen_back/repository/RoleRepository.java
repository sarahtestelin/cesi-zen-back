package com.cesi_zen_back.cesi_zen_back.repository;

import com.cesi_zen_back.cesi_zen_back.entity.Role;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, UUID> {

  Optional<Role> findByRoleName(String roleName);
}
