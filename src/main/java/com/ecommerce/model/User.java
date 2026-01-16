package com.ecommerce.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;  
import java.util.Collections;
import jakarta.persistence.ManyToOne; 
import jakarta.persistence.Table;

@Data
@Entity
@Table(name = "users")
public class User implements UserDetails {

  @Id @GeneratedValue
  private Long id;

  private String username;
  private String email;
  private String password;

  @ManyToOne
  private Role role;

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    if (role == null || role.getPermissions() == null) {
        return Collections.emptyList(); // Devuelve una lista vacía si no hay rol
    }

    return role.getPermissions().stream()
      .map(p -> (GrantedAuthority) p::getName)
      .toList();
  }

  @Override public boolean isAccountNonExpired() { return true; }
  @Override public boolean isAccountNonLocked() { return true; }
  @Override public boolean isCredentialsNonExpired() { return true; }
  @Override public boolean isEnabled() { return true; }
}
