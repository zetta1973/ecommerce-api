package com.ecommerce.security;

import com.ecommerce.model.User;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;
  
@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {

  @Override
  public boolean hasPermission(Authentication auth, Object targetDomainObject, Object permission) {
    if (auth.getPrincipal() instanceof User user) {
      return user.getAuthorities().stream()
          .anyMatch(a -> a.getAuthority().equals(permission.toString()));
    }
    return false;
  }

  @Override
  public boolean hasPermission(Authentication auth, Serializable targetId, String targetType, Object permission) {
    return hasPermission(auth, null, permission);
  }
}
