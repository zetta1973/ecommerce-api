package com.ecommerce.controller;

import com.ecommerce.model.User;
import com.ecommerce.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);
    private final UserRepository userRepo;

    public AdminController(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @GetMapping("/ping")
    public String ping() {
        log.info("Entrando en /admin/ping");
        return "pong";
    }

    @PreAuthorize("hasPermission(null, 'READ_USERS')")
    @GetMapping("/users")
    public List<User> listUsers() {
        log.info("Entrando en /admin/users - Intentando listar usuarios");
        List<User> users = userRepo.findAll();
        log.info("Se encontraron {} usuarios", users.size());
        return users;
    }
}
