package com.cesi_zen_back.cesi_zen_back.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "app_user")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class AppUser {

    @Id
    @GeneratedValue
    @Column(name = "app_user_id", nullable = false)
    private UUID id;

    @Column(name = "mail", nullable = false, unique = true, length = 150)
    private String mail;

    @Column(name = "pseudo", nullable = false, unique = true, length = 150)
    private String pseudo;

    @Column(name = "app_user_is_active", nullable = false)
    private boolean appUserIsActive;

    @Column(name = "hashed_password", nullable = false, length = 250)
    private String hashedPassword;

    @Column(name = "last_connection_at")
    private LocalDateTime lastConnectionAt;
}