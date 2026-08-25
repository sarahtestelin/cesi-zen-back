package com.cesi_zen_back.cesi_zen_back.repository;

import com.cesi_zen_back.cesi_zen_back.entity.Ressource;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RessourceRepository
    extends JpaRepository<Ressource, UUID>, JpaSpecificationExecutor<Ressource> {}
