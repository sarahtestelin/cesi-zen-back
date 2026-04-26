package com.cesi_zen_back.cesi_zen_back.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "diagnostic_question")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosticQuestion {

    @Id
    @GeneratedValue
    @Column(name = "diagnostic_question_id", nullable = false)
    private UUID id;

    @Column(name = "question", nullable = false, length = 255)
    private String question;

    @Column(name = "score", nullable = false)
    private int score;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
        active = true;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}