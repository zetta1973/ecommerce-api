package com.ecommerce.controller;

import com.ecommerce.model.User;
import com.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminControllerTest {

    private MockMvc mockMvc;
    private AdminController adminController;
    private UserRepository userRepo;

    @BeforeEach
    void setUp() {
        userRepo = Mockito.mock(UserRepository.class);
        adminController = new AdminController(userRepo);
        mockMvc = MockMvcBuilders.standaloneSetup(adminController).build();
    }

    @Test
    void shouldReturnPong() throws Exception {
        mockMvc.perform(get("/admin/ping"))
                .andExpect(status().isOk())
                .andExpect(content().string("pong"));
    }

    @Test
    void shouldListAllUsers() throws Exception {
        User user1 = new User();
        user1.setId(1L);
        user1.setEmail("user1@example.com");

        User user2 = new User();
        user2.setId(2L);
        user2.setEmail("user2@example.com");

        when(userRepo.findAll()).thenReturn(List.of(user1, user2));

        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnEmptyListWhenNoUsers() throws Exception {
        when(userRepo.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk());
    }
}
