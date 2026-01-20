package com.ecommerce.config;

import com.ecommerce.security.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
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
                .requestMatchers("/products").permitAll()

                // Protected endpoints
                .requestMatchers("/admin/users").hasAuthority("READ_USERS")
                .requestMatchers("/api/products").hasAuthority("CREATE_PRODUCTS")
                .requestMatchers("/api/products/**").hasAnyAuthority("UPDATE_PRODUCTS", "DELETE_PRODUCTS")

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
