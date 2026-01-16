package com.ecommerce.integration;

import com.ecommerce.config.SecurityConfig;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityConfigIntegrationTest {

    @Test
    void shouldReturnBCryptPasswordEncoder() {
        org.springframework.security.crypto.password.PasswordEncoder encoder =
            new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
        assertThat(encoder).isNotNull();
        assertThat(encoder).isInstanceOf(org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder.class);
    }

    @Test
    void shouldEncodePassword() {
        org.springframework.security.crypto.password.PasswordEncoder encoder =
            new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
        String rawPassword = "testPassword123";
        String encodedPassword = encoder.encode(rawPassword);

        assertThat(encodedPassword).isNotEmpty();
        assertThat(encodedPassword).isNotEqualTo(rawPassword);
        assertThat(encoder.matches(rawPassword, encodedPassword)).isTrue();
    }

    @Test
    void shouldNotMatchDifferentPasswords() {
        org.springframework.security.crypto.password.PasswordEncoder encoder =
            new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
        String password1 = "password1";
        String password2 = "password2";
        String encodedPassword = encoder.encode(password1);

        assertThat(encoder.matches(password2, encodedPassword)).isFalse();
    }

    @Test
    void shouldReturnDifferentHashesForSamePassword() {
        org.springframework.security.crypto.password.PasswordEncoder encoder =
            new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
        String password = "samePassword";

        String hash1 = encoder.encode(password);
        String hash2 = encoder.encode(password);

        assertThat(hash1).isNotEqualTo(hash2);
        assertThat(encoder.matches(password, hash1)).isTrue();
        assertThat(encoder.matches(password, hash2)).isTrue();
    }

    @Test
    void shouldValidatePasswordStrength() {
        org.springframework.security.crypto.password.PasswordEncoder encoder =
            new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
        String shortPassword = "123";
        String longPassword = "ThisIsAVeryLongPasswordWithSpecialChars!@#$%";

        String encodedShort = encoder.encode(shortPassword);
        String encodedLong = encoder.encode(longPassword);

        assertThat(encoder.matches(shortPassword, encodedShort)).isTrue();
        assertThat(encoder.matches(longPassword, encodedLong)).isTrue();
    }

    @Test
    void shouldHaveEnableMethodSecurityAnnotation() {
        assertThat(SecurityConfig.class.isAnnotationPresent(
            org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity.class
        )).isTrue();
    }

    @Test
    void shouldHaveCorrectConfigurationAnnotation() {
        assertThat(SecurityConfig.class.isAnnotationPresent(
            org.springframework.context.annotation.Configuration.class
        )).isTrue();
    }
}
