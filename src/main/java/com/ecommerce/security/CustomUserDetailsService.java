package com.ecommerce.security;

import com.ecommerce.model.User;
import com.ecommerce.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

  private static final Logger log = LoggerFactory.getLogger(CustomUserDetailsService.class);
  private final UserRepository userRepo;

  public CustomUserDetailsService(UserRepository userRepo) {
    this.userRepo = userRepo;
    log.info("CustomUserDetailsService instantiated");
  }

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    log.info("loadUserByUsername called with email: {}", email);
    User user = userRepo.findByEmail(email)
        .orElseThrow(() -> {
          log.warn("User not found: {}", email);
          return new UsernameNotFoundException("Usuario no encontrado: " + email);
        });
    log.info("User found: {}, password length: {}", user.getEmail(), user.getPassword().length());
    return user;
  }
}
