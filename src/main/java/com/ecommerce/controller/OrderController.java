package com.ecommerce.controller;

import com.ecommerce.dto.OrderRequestDto;
import com.ecommerce.dto.OrderResponseDto;
import com.ecommerce.model.User;
import com.ecommerce.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PreAuthorize("hasAuthority('CREATE_ORDERS')")
    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody OrderRequestDto dto) {
        return ResponseEntity.status(201).body(orderService.createOrder(user, dto));
    }

    @PreAuthorize("hasAuthority('READ_OWN_ORDERS')")
    @GetMapping
    public List<OrderResponseDto> getUserOrders(@AuthenticationPrincipal User user) {
        return orderService.getUserOrders(user);
    }

    @PreAuthorize("hasAuthority('READ_ALL_ORDERS')")
    @GetMapping("/all")
    public List<OrderResponseDto> getAllOrders() {
        return orderService.getAllOrders();
    }

    @PreAuthorize("hasAuthority('UPDATE_ORDER_STATUS')")
    @PutMapping("/{id}/status")
    public ResponseEntity<OrderResponseDto> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, status));
    }

    @PreAuthorize("hasAuthority('READ_USER_ORDERS')")
    @GetMapping("/user/{userId}")
    public List<OrderResponseDto> getOrdersByUser(@PathVariable Long userId) {
        return orderService.getOrdersByUserId(userId);
    }
}
