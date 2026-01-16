package com.ecommerce.security;

import java.util.Set;

public enum Role {
  USER(Set.of(Permission.VIEW_ORDERS)),
  ADMIN(Set.of(Permission.READ_USERS, Permission.MANAGE_ORDERS, Permission.CREATE_PRODUCTS, Permission.DELETE_PRODUCTS));

  private final Set<Permission> permissions;

  Role(Set<Permission> permissions) {
    this.permissions = permissions;
  }

  public Set<Permission> getPermissions() {
    return permissions;
  }
}
