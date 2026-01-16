package com.ecommerce.security;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RoleTest {

    @Test
    void shouldHaveCorrectPermissionsForUSER() {
        Set<Permission> permissions = Role.USER.getPermissions();

        assertThat(permissions).contains(Permission.VIEW_ORDERS);
        assertThat(permissions).hasSize(1);
    }

    @Test
    void shouldHaveCorrectPermissionsForADMIN() {
        Set<Permission> permissions = Role.ADMIN.getPermissions();

        assertThat(permissions).contains(
            Permission.READ_USERS,
            Permission.MANAGE_ORDERS,
            Permission.CREATE_PRODUCTS,
            Permission.DELETE_PRODUCTS
        );
        assertThat(permissions).hasSize(4);
    }

    @Test
    void shouldHaveExactlyTwoRoles() {
        assertThat(Role.values()).hasSize(2);
    }
}

class PermissionTest {

    @Test
    void shouldHaveAllRequiredPermissions() {
        assertThat(Permission.values()).containsExactly(
            Permission.READ_USERS,
            Permission.CREATE_PRODUCTS,
            Permission.DELETE_PRODUCTS,
            Permission.VIEW_ORDERS,
            Permission.MANAGE_ORDERS
        );
    }

    @Test
    void shouldReturnCorrectNameForREAD_USERS() {
        assertThat(Permission.READ_USERS.name()).isEqualTo("READ_USERS");
    }

    @Test
    void shouldReturnCorrectNameForCREATE_PRODUCTS() {
        assertThat(Permission.CREATE_PRODUCTS.name()).isEqualTo("CREATE_PRODUCTS");
    }
}
