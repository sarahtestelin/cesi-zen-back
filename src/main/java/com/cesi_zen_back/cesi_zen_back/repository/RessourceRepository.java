package com.cesi_zen_back.cesi_zen_back.repository;

import com.cesi_zen_back.cesi_zen_back.entity.Ressource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RessourceRepository extends JpaRepository<Ressource, UUID> {}