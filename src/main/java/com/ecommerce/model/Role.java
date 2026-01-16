package com.ecommerce.model;

import jakarta.persistence.*;
import java.util.Set;
import lombok.Data;

@Data
@Entity
public class Role {
  @Id @GeneratedValue
  private Long id;

  @Column(unique = true)
  private String name;

  @ManyToMany(fetch = FetchType.EAGER)
  private Set<Permission> permissions;

  // Getters and setters
}
