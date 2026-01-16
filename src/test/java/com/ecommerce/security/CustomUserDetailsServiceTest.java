package com.ecommerce.security;

import com.ecommerce.model.Permission;
import com.ecommerce.model.Role;
import com.ecommerce.model.User;
import com.ecommerce.repository.PermissionRepository;
import com.ecommerce.repository.RoleRepository;
import com.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepo;

    @Mock
    private RoleRepository roleRepo;

    @Mock
    private PermissionRepository permRepo;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    private User testUser;

    @BeforeEach
    void setUp() {
        Permission perm1 = new Permission();
        perm1.setId(1L);
        perm1.setName("READ_PRODUCTS");

        Permission perm2 = new Permission();
        perm2.setId(2L);
        perm2.setName("WRITE_PRODUCTS");

        Role role = new Role();
        role.setId(1L);
        role.setName("USER");
        role.setPermissions(Set.of(perm1, perm2));

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setRole(role);
    }

    @Test
    void shouldLoadUserByUsername() {
        when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        var userDetails = (User) userDetailsService.loadUserByUsername("test@example.com");

        assertThat(userDetails.getEmail()).isEqualTo("test@example.com");
        assertThat(userDetails.getPassword()).isEqualTo("encodedPassword");
        assertThat(userDetails.getRole()).isNotNull();
        assertThat(userDetails.getRole().getName()).isEqualTo("USER");
        assertThat(userDetails.getAuthorities()).hasSize(2);
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        when(userRepo.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("notfound@example.com"))
            .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void shouldMapPermissionsToAuthorities() {
        when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        var userDetails = (User) userDetailsService.loadUserByUsername("test@example.com");

        assertThat(userDetails.getAuthorities()).isNotEmpty();
        assertThat(userDetails.getAuthorities()).hasSize(2);
    }

    @Test
    void shouldHandleUserWithNoPermissions() {
        Role role = new Role();
        role.setId(1L);
        role.setName("USER");
        role.setPermissions(Set.of());

        testUser.setRole(role);

        when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        var userDetails = (User) userDetailsService.loadUserByUsername("test@example.com");

        assertThat(userDetails.getAuthorities()).isEmpty();
    }
}
