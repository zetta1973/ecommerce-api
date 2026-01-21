package com.ecommerce.controller;

import com.ecommerce.dto.RefreshTokenRequest;
import com.ecommerce.dto.UserLoginDto;
import com.ecommerce.dto.UserRegisterDto;
import com.ecommerce.model.Role;
import com.ecommerce.model.User;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashSet;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.matches;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest {

    private MockMvc mockMvc;
    private AuthController authController;
    private UserRepository userRepo;
    private PasswordEncoder encoder;
    private ObjectMapper objectMapper;

    private User testUser;

    @BeforeEach
    void setUp() {
        userRepo = Mockito.mock(UserRepository.class);
        encoder = Mockito.mock(PasswordEncoder.class);
        objectMapper = new ObjectMapper();
        authController = new AuthController(userRepo, encoder);
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();

        Role role = new Role();
        role.setId(1L);
        role.setName("USER");
        role.setPermissions(new HashSet<>());

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setUsername("testuser");
        testUser.setRole(role);
    }

    @Test
    void shouldRegisterUser() throws Exception {
        when(userRepo.findByEmail("newuser@example.com")).thenReturn(Optional.empty());
        when(encoder.encode("password123")).thenReturn("encodedPassword");

        UserRegisterDto dto = new UserRegisterDto();
        dto.setUsername("newuser");
        dto.setEmail("newuser@example.com");
        dto.setPassword("password123");

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldReturnBadRequestWhenEmailExists() throws Exception {
        when(userRepo.findByEmail("existing@example.com")).thenReturn(Optional.of(testUser));

        UserRegisterDto dto = new UserRegisterDto();
        dto.setUsername("newuser");
        dto.setEmail("existing@example.com");
        dto.setPassword("password123");

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void shouldLoginSuccessfully() throws Exception {
        when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(encoder.matches("password", "encodedPassword")).thenReturn(true);

        UserLoginDto dto = new UserLoginDto();
        dto.setEmail("test@example.com");
        dto.setPassword("password");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }

    @Test
    void shouldReturnUnauthorizedWhenLoginFails() throws Exception {
        when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(encoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);

        UserLoginDto dto = new UserLoginDto();
        dto.setEmail("test@example.com");
        dto.setPassword("wrongpassword");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void shouldReturnUnauthorizedWhenUserNotFound() throws Exception {
        when(userRepo.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        UserLoginDto dto = new UserLoginDto();
        dto.setEmail("notfound@example.com");
        dto.setPassword("password");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void shouldRefreshTokenSuccessfully() throws Exception {
        when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        String validRefreshToken = JwtUtil.generateRefreshToken(testUser);

        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken(validRefreshToken);

        mockMvc.perform(post("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void shouldReturnUnauthorizedWhenRefreshTokenInvalid() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("invalid.refresh.token");

        mockMvc.perform(post("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").exists());
    }
}
