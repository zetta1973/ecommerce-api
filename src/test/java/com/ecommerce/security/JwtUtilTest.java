package com.ecommerce.security;

import com.ecommerce.model.Permission;
import com.ecommerce.model.Role;
import com.ecommerce.model.User;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    @Test
    void shouldGenerateToken() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPassword("password");

        Role role = new Role();
        role.setId(1L);
        role.setName("USER");
        role.setPermissions(new HashSet<>());

        Permission perm = new Permission();
        perm.setId(1L);
        perm.setName("READ_PRODUCTS");
        role.setPermissions(Set.of(perm));

        user.setRole(role);

        String token = JwtUtil.generateToken(user);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token).isNotEqualTo("test");
    }

    @Test
    void shouldGenerateRefreshToken() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        String refreshToken = JwtUtil.generateRefreshToken(user);

        assertThat(refreshToken).isNotNull();
        assertThat(refreshToken).isNotEmpty();
        assertThat(refreshToken).isNotEqualTo("test");
    }

    @Test
    void shouldExtractEmailFromToken() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password");

        Role role = new Role();
        role.setName("USER");
        role.setPermissions(new HashSet<>());

        user.setRole(role);

        String token = JwtUtil.generateToken(user);
        String email = JwtUtil.extractEmail(token);

        assertThat(email).isEqualTo("test@example.com");
    }

    @Test
    void shouldExtractEmailFromRefreshToken() {
        User user = new User();
        user.setEmail("test@example.com");

        String refreshToken = JwtUtil.generateRefreshToken(user);
        String email = JwtUtil.extractEmailFromRefreshToken(refreshToken);

        assertThat(email).isEqualTo("test@example.com");
    }

    @Test
    void shouldExtractRoleFromToken() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password");

        Role role = new Role();
        role.setName("ADMIN");
        role.setPermissions(new HashSet<>());

        user.setRole(role);

        String token = JwtUtil.generateToken(user);
        String roleExtracted = JwtUtil.extractRole(token);

        assertThat(roleExtracted).isEqualTo("ADMIN");
    }

    @Test
    void shouldValidateValidToken() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password");

        Role role = new Role();
        role.setName("USER");
        role.setPermissions(new HashSet<>());

        user.setRole(role);

        String token = JwtUtil.generateToken(user);
        boolean isValid = JwtUtil.validateToken(token);

        assertThat(isValid).isTrue();
    }

    @Test
    void shouldValidateInvalidToken() {
        boolean isValid = JwtUtil.validateToken("invalid.token.here");

        assertThat(isValid).isFalse();
    }

    @Test
    void shouldValidateValidRefreshToken() {
        User user = new User();
        user.setEmail("test@example.com");

        String refreshToken = JwtUtil.generateRefreshToken(user);
        boolean isValid = JwtUtil.validateRefreshToken(refreshToken);

        assertThat(isValid).isTrue();
    }

    @Test
    void shouldValidateInvalidRefreshToken() {
        boolean isValid = JwtUtil.validateRefreshToken("invalid.refresh.token");

        assertThat(isValid).isFalse();
    }

    @Test
    void shouldReturnExpirationTime() {
        long expiration = JwtUtil.getExpirationTime();

        assertThat(expiration).isEqualTo(86400000L);
    }
}
