package com.cesi_zen_back.cesi_zen_back.repository;

import com.cesi_zen_back.cesi_zen_back.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppUserRepository extends JpaRepository<AppUser, UUID> {

    Optional<AppUser> findByMail(String mail);

    boolean existsByMail(String mail);

    boolean existsByPseudo(String pseudo);

    List<AppUser> findByIsActiveTrueAndLastConnexionBefore(LocalDateTime limitDate);

    long countByIsActiveTrue();
}