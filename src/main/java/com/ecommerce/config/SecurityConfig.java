package com.ecommerce.config;

import com.ecommerce.security.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.http.HttpMethod;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
@Profile("!ci")
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.csrf(cs -> cs.disable())
            .authorizeHttpRequests(auth -> auth
                // Health check endpoints for Kubernetes probes - permit ALL actuator paths first
                .requestMatchers("/actuator/**", "/actuator", "/health", "/health/**").permitAll()

                // Public endpoints
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/admin/ping").permitAll()
                .requestMatchers("/api/products").permitAll()
                .requestMatchers("/api/products/search").permitAll()

                // Protected endpoints
                .requestMatchers("/admin/users").hasAuthority("READ_USERS")
                .requestMatchers("/api/products/{id}").hasAuthority("READ_PRODUCTS")
                .requestMatchers("/api/products/{id}/stock").hasAuthority("READ_PRODUCT_STOCK")
                .requestMatchers(HttpMethod.POST, "/api/products").hasAuthority("CREATE_PRODUCTS")
                .requestMatchers(HttpMethod.PUT, "/api/products/**").hasAuthority("UPDATE_PRODUCTS")
                .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasAuthority("DELETE_PRODUCTS")

                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
