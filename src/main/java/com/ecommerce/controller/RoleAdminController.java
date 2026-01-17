package com.ecommerce.controller;

import com.ecommerce.dto.AssignPermissionDto;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.Permission;  
import com.ecommerce.model.Role;
import com.ecommerce.repository.PermissionRepository;
import com.ecommerce.repository.RoleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/admin/roles")
public class RoleAdminController {

  private final RoleRepository roleRepo;
  private final PermissionRepository permRepo;

  public RoleAdminController(RoleRepository roleRepo, PermissionRepository permRepo) {
    this.roleRepo = roleRepo;
    this.permRepo = permRepo;
  }

  @PostMapping("/assign")
  public ResponseEntity<?> assignPermission(@RequestBody AssignPermissionDto dto) {
    Role role = roleRepo.findByName(dto.getRoleName())
            .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + dto.getRoleName()));
    Permission perm = permRepo.findByName(dto.getPermissionName())
            .orElseThrow(() -> new ResourceNotFoundException("Permission not found: " + dto.getPermissionName()));
    role.getPermissions().add(perm);
    roleRepo.save(role);
    return ResponseEntity.ok().build();
  }
}
