package com.cesi_zen_back.cesi_zen_back.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "survey_result")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosticResult {

    @Id
    @GeneratedValue
    @Column(name = "survey_result_id", nullable = false)
    private UUID id;

    @Column(name = "final_score", nullable = false)
    private int finalScore;

    @Column(name = "level", nullable = false, length = 50)
    private String level;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_user_id")
    private AppUser appUser;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}