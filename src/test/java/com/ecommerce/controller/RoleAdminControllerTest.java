package com.ecommerce.controller;

import com.ecommerce.config.GlobalExceptionHandler;
import com.ecommerce.dto.AssignPermissionDto;
import com.ecommerce.model.Permission;
import com.ecommerce.model.Role;
import com.ecommerce.repository.PermissionRepository;
import com.ecommerce.repository.RoleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashSet;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RoleAdminControllerTest {

    private MockMvc mockMvc;
    private RoleAdminController roleAdminController;
    private RoleRepository roleRepo;
    private PermissionRepository permRepo;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        roleRepo = Mockito.mock(RoleRepository.class);
        permRepo = Mockito.mock(PermissionRepository.class);
        objectMapper = new ObjectMapper();
        roleAdminController = new RoleAdminController(roleRepo, permRepo);
        mockMvc = MockMvcBuilders.standaloneSetup(roleAdminController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldAssignPermissionToRole() throws Exception {
        Role role = new Role();
        role.setId(1L);
        role.setName("ADMIN");
        role.setPermissions(new HashSet<>());

        Permission perm = new Permission();
        perm.setId(1L);
        perm.setName("READ_USERS");

        when(roleRepo.findByName("ADMIN")).thenReturn(Optional.of(role));
        when(permRepo.findByName("READ_USERS")).thenReturn(Optional.of(perm));
        when(roleRepo.save(any(Role.class))).thenReturn(role);

        AssignPermissionDto dto = new AssignPermissionDto();
        dto.setRoleName("ADMIN");
        dto.setPermissionName("READ_USERS");

        mockMvc.perform(post("/admin/roles/assign")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnNotFoundWhenRoleDoesNotExist() throws Exception {
        when(roleRepo.findByName("NONEXISTENT")).thenReturn(Optional.empty());

        AssignPermissionDto dto = new AssignPermissionDto();
        dto.setRoleName("NONEXISTENT");
        dto.setPermissionName("READ_USERS");

        mockMvc.perform(post("/admin/roles/assign")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotFoundWhenPermissionDoesNotExist() throws Exception {
        Role role = new Role();
        role.setId(1L);
        role.setName("ADMIN");

        when(roleRepo.findByName("ADMIN")).thenReturn(Optional.of(role));
        when(permRepo.findByName("NONEXISTENT")).thenReturn(Optional.empty());

        AssignPermissionDto dto = new AssignPermissionDto();
        dto.setRoleName("ADMIN");
        dto.setPermissionName("NONEXISTENT");

        mockMvc.perform(post("/admin/roles/assign")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }
}
