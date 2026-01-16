package com.ecommerce.security;

import com.ecommerce.model.Permission;
import com.ecommerce.model.Role;
import com.ecommerce.model.User;
import com.ecommerce.repository.PermissionRepository;
import com.ecommerce.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomPermissionEvaluatorTest {

    @Mock
    private PermissionRepository permRepo;

    @Mock
    private RoleRepository roleRepo;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CustomPermissionEvaluator evaluator;

    private User testUser;

    @BeforeEach
    void setUp() {
        Permission perm1 = new Permission();
        perm1.setId(1L);
        perm1.setName("READ_USERS");

        Permission perm2 = new Permission();
        perm2.setId(2L);
        perm2.setName("WRITE_USERS");

        Role role = new Role();
        role.setId(1L);
        role.setName("ADMIN");
        role.setPermissions(Set.of(perm1, perm2));

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("admin@example.com");
        testUser.setRole(role);
    }

    @Test
    void shouldGrantPermissionWhenUserHasIt() {
        when(authentication.getPrincipal()).thenReturn(testUser);

        boolean hasPermission = evaluator.hasPermission(authentication, null, "READ_USERS");

        assertThat(hasPermission).isTrue();
    }

    @Test
    void shouldDenyPermissionWhenUserDoesNotHaveIt() {
        when(authentication.getPrincipal()).thenReturn(testUser);

        boolean hasPermission = evaluator.hasPermission(authentication, null, "DELETE_USERS");

        assertThat(hasPermission).isFalse();
    }

    @Test
    void shouldDenyWhenPermissionStringContainsMultiple() {
        when(authentication.getPrincipal()).thenReturn(testUser);

        boolean hasPermission = evaluator.hasPermission(authentication, null, "READ_USERS,WRITE_PRODUCTS");

        assertThat(hasPermission).isFalse();
    }

    @Test
    void shouldGrantPermissionWhenUserHasExactPermission() {
        when(authentication.getPrincipal()).thenReturn(testUser);

        boolean hasPermission = evaluator.hasPermission(authentication, null, "READ_USERS");

        assertThat(hasPermission).isTrue();
    }

    @Test
    void shouldGrantPermissionWhenUserHasMatchingPermission() {
        when(authentication.getPrincipal()).thenReturn(testUser);

        boolean hasPermission = evaluator.hasPermission(authentication, null, "WRITE_USERS");

        assertThat(hasPermission).isTrue();
    }

    @Test
    void shouldHandleUserWithNoPermissions() {
        Role role = new Role();
        role.setId(2L);
        role.setName("GUEST");
        role.setPermissions(Set.of());

        testUser.setRole(role);
        when(authentication.getPrincipal()).thenReturn(testUser);

        boolean hasPermission = evaluator.hasPermission(authentication, null, "READ_USERS");

        assertThat(hasPermission).isFalse();
    }

    @Test
    void shouldReturnFalseWhenAuthenticationIsNull() {
        when(authentication.getPrincipal()).thenReturn(null);

        boolean hasPermission = evaluator.hasPermission(authentication, null, "READ_USERS");

        assertThat(hasPermission).isFalse();
    }

    @Test
    void shouldHandleUserWithNullRole() {
        testUser.setRole(null);
        when(authentication.getPrincipal()).thenReturn(testUser);

        boolean hasPermission = evaluator.hasPermission(authentication, null, "READ_USERS");

        assertThat(hasPermission).isFalse();
    }
}
