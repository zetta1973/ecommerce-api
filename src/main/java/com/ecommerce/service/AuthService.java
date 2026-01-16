package com.ecommerce.service;

import com.ecommerce.model.User;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
  private final UserRepository userRepo;
  private final PasswordEncoder encoder;

  public AuthService(UserRepository userRepo, PasswordEncoder encoder) {
    this.userRepo = userRepo;
    this.encoder = encoder;
  }

  public User register(User user) {
    user.setPassword(encoder.encode(user.getPassword()));
    return userRepo.save(user);
  }

  public String login(String email, String password) {
    User user = userRepo.findByEmail(email).orElseThrow();
    if (encoder.matches(password, user.getPassword())) {
      return JwtUtil.generateToken(user);
    }
    throw new RuntimeException("Invalid credentials");
  }
}
