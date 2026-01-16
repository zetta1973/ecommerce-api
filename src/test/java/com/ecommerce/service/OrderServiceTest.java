package com.ecommerce.service;

import com.ecommerce.dto.OrderRequestDto;
import com.ecommerce.dto.OrderResponseDto;
import com.ecommerce.kafka.KafkaProducer;
import com.ecommerce.kafka.OrderCreatedEvent;
import com.ecommerce.model.Order;
import com.ecommerce.model.Product;
import com.ecommerce.model.User;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepo;

    @Mock
    private ProductRepository productRepo;

    @Mock
    private KafkaProducer kafkaProducer;

    @InjectMocks
    private OrderService orderService;

    @Test
    void shouldCreateOrder() {
        User user = new User();
        user.setId(1L);

        Product product = new Product();
        product.setId(10L);
        product.setStock(10);
        product.setPrice(new java.math.BigDecimal("100.00"));

        when(productRepo.findAllById(List.of(10L))).thenReturn(List.of(product));
        when(orderRepo.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderRequestDto dto = new OrderRequestDto();
        dto.setProductIds(List.of(10L));

        OrderResponseDto response = orderService.createOrder(user, dto);

        assertThat(response.getUserId()).isEqualTo(user.getId());
        assertThat(response.getProducts()).hasSize(1);
        assertThat(response.getProducts().get(0).getId()).isEqualTo(product.getId());
        assertThat(response.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());

        verify(productRepo).findAllById(List.of(10L));
        verify(orderRepo).save(any(Order.class));
    }

    @Test
    void shouldThrowExceptionWhenProductNotFound() {
        User user = new User();
        user.setId(1L);

        Product product = new Product();
        product.setId(10L);
        product.setStock(10);
        product.setPrice(new BigDecimal("100.00"));

        when(productRepo.findAllById(List.of(10L, 20L))).thenReturn(List.of(product));

        OrderRequestDto dto = new OrderRequestDto();
        dto.setProductIds(Arrays.asList(10L, 20L));

        assertThatThrownBy(() -> orderService.createOrder(user, dto))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Some products not found");
    }

    @Test
    void shouldThrowExceptionWhenProductIsOutOfStock() {
        User user = new User();
        user.setId(1L);

        Product product = new Product();
        product.setId(10L);
        product.setName("Test Product");
        product.setStock(0);
        product.setPrice(new BigDecimal("100.00"));

        when(productRepo.findAllById(List.of(10L))).thenReturn(List.of(product));

        OrderRequestDto dto = new OrderRequestDto();
        dto.setProductIds(List.of(10L));

        assertThatThrownBy(() -> orderService.createOrder(user, dto))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("is out of stock");
    }

    @Test
    void shouldCalculateCorrectTotal() {
        User user = new User();
        user.setId(1L);

        Product product1 = new Product();
        product1.setId(1L);
        product1.setStock(10);
        product1.setPrice(new BigDecimal("100.00"));

        Product product2 = new Product();
        product2.setId(2L);
        product2.setStock(5);
        product2.setPrice(new BigDecimal("50.00"));

        when(productRepo.findAllById(Arrays.asList(1L, 2L))).thenReturn(Arrays.asList(product1, product2));
        when(orderRepo.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderRequestDto dto = new OrderRequestDto();
        dto.setProductIds(Arrays.asList(1L, 2L));

        OrderResponseDto response = orderService.createOrder(user, dto);

        assertThat(response.getTotal()).isEqualTo(150.00);
    }

    @Test
    void shouldSendEventToKafka() {
        User user = new User();
        user.setId(1L);

        Product product = new Product();
        product.setId(10L);
        product.setStock(10);
        product.setPrice(new BigDecimal("100.00"));

        when(productRepo.findAllById(List.of(10L))).thenReturn(List.of(product));
        when(orderRepo.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(100L);
            return order;
        });

        OrderRequestDto dto = new OrderRequestDto();
        dto.setProductIds(List.of(10L));

        orderService.createOrder(user, dto);

        ArgumentCaptor<OrderCreatedEvent> eventCaptor = ArgumentCaptor.forClass(OrderCreatedEvent.class);
        verify(kafkaProducer).publishOrderCreated(eventCaptor.capture());

        OrderCreatedEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getOrderId()).isEqualTo(100L);
        assertThat(capturedEvent.getUserId()).isEqualTo(1L);
        assertThat(capturedEvent.getTotal()).isEqualTo(100.00);
    }

    @Test
    void shouldReturnUserOrders() {
        User user = new User();
        user.setId(1L);

        Product product = new Product();
        product.setId(1L);

        Order order = new Order();
        order.setId(1L);
        order.setUser(user);
        order.setProducts(List.of(product));

        when(orderRepo.findByUserId(1L)).thenReturn(List.of(order));

        List<OrderResponseDto> orders = orderService.getUserOrders(user);

        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getUserId()).isEqualTo(1L);
    }

    @Test
    void shouldReturnAllOrders() {
        User user = new User();
        user.setId(1L);

        Product product = new Product();
        product.setId(1L);

        Order order1 = new Order();
        order1.setId(1L);
        order1.setUser(user);
        order1.setProducts(List.of(product));
        Order order2 = new Order();
        order2.setId(2L);
        order2.setUser(user);
        order2.setProducts(List.of(product));

        when(orderRepo.findAll()).thenReturn(Arrays.asList(order1, order2));

        List<OrderResponseDto> orders = orderService.getAllOrders();

        assertThat(orders).hasSize(2);
    }

    @Test
    void shouldUpdateOrderStatus() {
        User user = new User();
        user.setId(1L);

        Product product = new Product();
        product.setId(1L);

        Order order = new Order();
        order.setId(1L);
        order.setStatus("PENDING");
        order.setUser(user);
        order.setProducts(List.of(product));

        when(orderRepo.findById(1L)).thenReturn(java.util.Optional.of(order));
        when(orderRepo.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponseDto updated = orderService.updateOrderStatus(1L, "COMPLETED");

        assertThat(updated.getStatus()).isEqualTo("COMPLETED");
    }

    @Test
    void shouldThrowExceptionWhenOrderNotFoundForUpdate() {
        when(orderRepo.findById(999L)).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> orderService.updateOrderStatus(999L, "COMPLETED"))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Order not found");
    }

    @Test
    void shouldReturnOrdersByUserId() {
        User user = new User();
        user.setId(1L);

        Product product = new Product();
        product.setId(1L);

        Order order = new Order();
        order.setId(1L);
        order.setUser(user);
        order.setProducts(List.of(product));

        when(orderRepo.findByUserId(1L)).thenReturn(List.of(order));

        List<OrderResponseDto> orders = orderService.getOrdersByUserId(1L);

        assertThat(orders).hasSize(1);
    }
}
