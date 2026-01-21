package com.ecommerce.controller;

import com.ecommerce.dto.UserLoginDto;
import com.ecommerce.dto.RefreshTokenRequest;
import com.ecommerce.dto.UserRegisterDto;
import com.ecommerce.model.User;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.security.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserRegisterDto dto) {
        log.info("Register attempt for email: {}", dto.getEmail());

        if (userRepo.findByEmail(dto.getEmail()).isPresent()) {
            log.warn("Email already exists: {}", dto.getEmail());
            return ResponseEntity.badRequest().body(Map.of("error", "Email already exists"));
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(encoder.encode(dto.getPassword()));
        userRepo.save(user);

        log.info("User registered successfully: {}", dto.getEmail());
        return ResponseEntity.status(201).build();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginDto dto) {
        log.info("Login attempt for email: {}", dto.getEmail());

        User user = userRepo.findByEmail(dto.getEmail()).orElse(null);

        if (user == null) {
            log.warn("User not found: {}", dto.getEmail());
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }

        boolean matches = encoder.matches(dto.getPassword(), user.getPassword());
        log.debug("Password match result: {}", matches);

        if (!matches) {
            log.warn("Invalid password for user: {}", dto.getEmail());
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }

        String token = JwtUtil.generateToken(user);
        String refreshToken = JwtUtil.generateRefreshToken(user);

        log.info("Login SUCCESS for user: {}", dto.getEmail());

        return ResponseEntity.ok(Map.of(
            "token", token,
            "refreshToken", refreshToken,
            "expiresIn", JwtUtil.JWT_EXPIRATION
        ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        if (JwtUtil.validateRefreshToken(request.getRefreshToken())) {
            String email = JwtUtil.extractEmailFromRefreshToken(request.getRefreshToken());
            User user = userRepo.findByEmail(email).orElse(null);
            if (user != null) {
                String newToken = JwtUtil.generateToken(user);
                return ResponseEntity.ok(Map.of("token", newToken));
            }
        }
        return ResponseEntity.status(401).body(Map.of("error", "Invalid refresh token"));
    }
}
