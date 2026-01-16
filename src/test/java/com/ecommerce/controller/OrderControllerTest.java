package com.ecommerce.controller;

import com.ecommerce.dto.OrderRequestDto;
import com.ecommerce.dto.OrderResponseDto;
import com.ecommerce.model.User;
import com.ecommerce.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderControllerTest {

    private MockMvc mockMvc;
    private OrderController orderController;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = Mockito.mock(OrderService.class);
        orderController = new OrderController(orderService);
        mockMvc = MockMvcBuilders.standaloneSetup(orderController).build();
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldCreateOrder() throws Exception {
        when(orderService.createOrder(any(User.class), any(OrderRequestDto.class)))
                .thenReturn(new OrderResponseDto());

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productIds\":[1,2]}"))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldGetUserOrders() throws Exception {
        when(orderService.getUserOrders(any(User.class))).thenReturn(List.of(new OrderResponseDto()));

        mockMvc.perform(get("/api/orders"))
               .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetAllOrders() throws Exception {
        when(orderService.getAllOrders()).thenReturn(List.of(new OrderResponseDto()));

        mockMvc.perform(get("/api/orders/all"))
               .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateOrderStatus() throws Exception {
        when(orderService.updateOrderStatus(1L, "SHIPPED")).thenReturn(new OrderResponseDto());

        mockMvc.perform(put("/api/orders/1/status")
                .param("status", "SHIPPED")
                .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetOrdersByUser() throws Exception {
        when(orderService.getOrdersByUserId(1L)).thenReturn(List.of(new OrderResponseDto()));

        mockMvc.perform(get("/api/orders/user/1"))
               .andExpect(status().isOk());
    }
}
