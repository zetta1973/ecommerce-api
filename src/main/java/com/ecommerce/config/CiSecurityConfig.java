package com.ecommerce.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collections;

@Configuration
@EnableWebSecurity
@Profile("ci")
public class CiSecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(cs -> cs.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/**", "/actuator", "/health", "/health/**").permitAll()
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/admin/ping").permitAll()
                .requestMatchers("/products").permitAll()
                .anyRequest().permitAll()
            )
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        return authentication -> null;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User.builder()
            .passwordEncoder(passwordEncoder()::encode)
            .username("ci-user")
            .password("ci-password")
            .authorities(Collections.emptyList())
            .build();
        return new InMemoryUserDetailsManager(user);
    }
}
