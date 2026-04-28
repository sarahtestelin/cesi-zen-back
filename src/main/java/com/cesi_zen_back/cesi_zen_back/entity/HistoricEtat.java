package com.cesi_zen_back.cesi_zen_back.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "historic_etat")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HistoricEtat {

    @Id
    @GeneratedValue
    @Column(name = "historic_etat_id", nullable = false)
    private UUID id;

    @Column(name = "modification_date", nullable = false)
    private LocalDateTime modificationDate;

    @Column(name = "old_value", nullable = false, columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", nullable = false, columnDefinition = "TEXT")
    private String newValue;

    @Column(name = "comment", nullable = false, length = 255)
    private String comment;

    @Column(name = "entity_type", length = 100)
    private String entityType;

    @Column(name = "entity_id")
    private UUID entityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_user_id")
    private AppUser appUser;

    @PrePersist
    public void prePersist() {
        if (modificationDate == null) {
            modificationDate = LocalDateTime.now();
        }
    }
}