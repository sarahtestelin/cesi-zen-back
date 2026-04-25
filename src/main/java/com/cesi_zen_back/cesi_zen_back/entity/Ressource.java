package com.cesi_zen_back.cesi_zen_back.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ressource")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Ressource {

    @Id
    @GeneratedValue
    @Column(name = "ressource_id", nullable = false)
    private UUID id;

    @Column(name = "ressource_is_active", nullable = false)
    private boolean ressourceIsActive;

    @Column(name = "ressource_is_used", nullable = false)
    private boolean ressourceIsUsed;

    @Column(name = "ressource_title", nullable = false, length = 150)
    private String title;

    @Column(name = "ressource_description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "status", nullable = false, length = 150)
    private String status;

    @Column(name = "ressource_created_at", nullable = false)
    private LocalDateTime createdAt;
}