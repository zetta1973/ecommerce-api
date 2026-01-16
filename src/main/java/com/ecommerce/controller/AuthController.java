package com.ecommerce.controller;

import com.ecommerce.dto.UserRegisterDto;
import com.ecommerce.dto.UserLoginDto;
import com.ecommerce.dto.RefreshTokenRequest;
import com.ecommerce.model.User;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final UserRepository userRepo;
    private final PasswordEncoder encoder;

    public AuthController(AuthenticationManager authManager, UserRepository userRepo, 
                         PasswordEncoder encoder) {
        this.authManager = authManager;
        this.userRepo = userRepo;
        this.encoder = encoder;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserRegisterDto dto) {
        if (userRepo.findByEmail(dto.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email already exists"));
        }
        
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(encoder.encode(dto.getPassword()));
        userRepo.save(user);
        return ResponseEntity.status(201).build();
    }

@PostMapping("/login")
public ResponseEntity<?> login(@RequestBody UserLoginDto dto) {
    try {
        Authentication auth = authManager.authenticate(
            new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword()));
        
        User user = (User) auth.getPrincipal();
        String token = JwtUtil.generateToken(user);
        String refreshToken = JwtUtil.generateRefreshToken(user);
        
        return ResponseEntity.ok(Map.of(
            "token", token,
            "refreshToken", refreshToken,
            "expiresIn", JwtUtil.JWT_EXPIRATION
        ));
    } catch (Exception e) {
        return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
    }
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
