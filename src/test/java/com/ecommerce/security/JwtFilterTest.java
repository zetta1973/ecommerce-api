package com.ecommerce.security;

import com.ecommerce.model.User;
import com.ecommerce.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JwtFilterTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private JwtFilter jwtFilter;

    @BeforeEach
    void setUp() {
        jwtFilter = new JwtFilter(userRepository);
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldNotSetAuthenticationWhenNoAuthHeader() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void shouldNotSetAuthenticationWhenInvalidAuthHeaderFormat() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("InvalidFormat");

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void shouldBeAnnotatedWithComponent() {
        assertThat(JwtFilter.class.isAnnotationPresent(org.springframework.stereotype.Component.class)).isTrue();
    }

    @Test
    void shouldExtendOncePerRequestFilter() {
        assertThat(JwtFilter.class.getSuperclass().getName()).isEqualTo("org.springframework.web.filter.OncePerRequestFilter");
    }

    @Test
    void shouldHaveConstructorWithDependencies() {
        UserRepository mockRepo = mock(UserRepository.class);
        JwtUtil mockJwtUtil = mock(JwtUtil.class);
        JwtFilter filter = new JwtFilter(mockRepo);
        assertThat(filter).isNotNull();
    }
}
