package com.cesi_zen_back.cesi_zen_back.mapper;

import com.cesi_zen_back.cesi_zen_back.dto.RessourceResponseDto;
import com.cesi_zen_back.cesi_zen_back.entity.Ressource;

public class RessourceMapper {

    private RessourceMapper() {
    }

    public static RessourceResponseDto toDto(Ressource ressource) {
        return new RessourceResponseDto(
                ressource.getId(),
                ressource.isRessourceIsActive(),
                ressource.isRessourceIsUsed(),
                ressource.getTitle(),
                ressource.getDescription(),
                ressource.getStatus(),
                ressource.getCategory(),
                ressource.getCreatedAt(),
                ressource.getUpdatedAt(),
                ressource.getVersion()
        );
    }
}