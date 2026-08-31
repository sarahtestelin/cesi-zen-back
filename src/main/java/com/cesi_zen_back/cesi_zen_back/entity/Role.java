package com.cesi_zen_back.cesi_zen_back.entity;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "role")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "role_id")
  private UUID roleId;

  @Column(name = "role_name", nullable = false, unique = true, length = 50)
  private String roleName;
}
