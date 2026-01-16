package com.ecommerce.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityConfigTest {

    @Test
    void shouldCreateBCryptPasswordEncoder() {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        assertThat(encoder).isNotNull();
        assertThat(encoder).isInstanceOf(BCryptPasswordEncoder.class);
    }

    @Test
    void shouldEncodePassword() {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "testPassword123";
        String encodedPassword = encoder.encode(rawPassword);

        assertThat(encodedPassword).isNotEmpty();
        assertThat(encodedPassword).isNotEqualTo(rawPassword);
        assertThat(encoder.matches(rawPassword, encodedPassword)).isTrue();
    }

    @Test
    void shouldNotMatchDifferentPasswords() {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        String password1 = "password1";
        String password2 = "password2";
        String encodedPassword = encoder.encode(password1);

        assertThat(encoder.matches(password2, encodedPassword)).isFalse();
    }

    @Test
    void shouldReturnDifferentHashesForSamePassword() {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "samePassword";

        String hash1 = encoder.encode(password);
        String hash2 = encoder.encode(password);

        assertThat(hash1).isNotEqualTo(hash2);
        assertThat(encoder.matches(password, hash1)).isTrue();
        assertThat(encoder.matches(password, hash2)).isTrue();
    }
}
