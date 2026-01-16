package com.ecommerce.service;

import com.ecommerce.model.User;
import com.ecommerce.repository.UserRepository;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepo;

    @Mock
    private PasswordEncoder encoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void shouldRegisterUser() {
        User user = new User();
        user.setPassword("raw");

        when(encoder.encode("raw")).thenReturn("encoded");
        when(userRepo.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = authService.register(user);

        assertThat(result.getPassword()).isEqualTo("encoded");
        verify(userRepo).save(user);
    }

    @Test
    void shouldLoginSuccessfully() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("encoded");

        when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(encoder.matches("raw", "encoded")).thenReturn(true);

        String token = authService.login("test@example.com", "raw");

        assertThat(token).isNotBlank();
        verify(userRepo).findByEmail("test@example.com");
    }

    @Test
    void shouldThrowIfUserNotFound() {
        when(userRepo.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
            authService.login("missing@example.com", "any")
        );

        verify(userRepo).findByEmail("missing@example.com");
    }

    @Test
    void shouldThrowIfPasswordMismatch() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("encoded");

        when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(encoder.matches("wrong", "encoded")).thenReturn(false);

        assertThrows(RuntimeException.class, () ->
            authService.login("test@example.com", "wrong")
        );

        verify(userRepo).findByEmail("test@example.com");
        verify(encoder).matches("wrong", "encoded");
    }
}
