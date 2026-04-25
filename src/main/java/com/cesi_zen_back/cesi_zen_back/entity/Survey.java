package com.cesi_zen_back.cesi_zen_back.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "survey")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Survey {

    @Id
    @GeneratedValue
    @Column(name = "survey_id", nullable = false)
    private UUID id;

    @Column(name = "question", nullable = false, length = 255)
    private String question;

    @Column(name = "score", nullable = false)
    private int score;

    @Column(name = "final_score", nullable = false)
    private int finalScore;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_user_id")
    private AppUser appUser;
}