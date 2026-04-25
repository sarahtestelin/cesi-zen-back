package com.cesi_zen_back.cesi_zen_back.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "reset_password")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResetPassword {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "reset_password_id")
    private UUID idResetPassword;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expiration_date", nullable = false)
    private LocalDateTime expirationDate;

    @Column(name = "token_demand_reset", nullable = false, unique = true, length = 255)
    private String tokenDemandReset;

    @Column(name = "used", nullable = false)
    private boolean used;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "app_user_id", nullable = false)
    private AppUser user;
}