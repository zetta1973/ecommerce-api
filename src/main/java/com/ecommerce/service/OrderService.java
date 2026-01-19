package com.ecommerce.service;

import com.ecommerce.dto.OrderRequestDto;
import com.ecommerce.dto.OrderResponseDto;
import com.ecommerce.dto.ProductResponseDto;
import com.ecommerce.kafka.KafkaProducer;
import com.ecommerce.kafka.OrderCreatedEvent;
import com.ecommerce.model.Order;
import com.ecommerce.model.Product;
import com.ecommerce.model.User;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepo;
    private final ProductRepository productRepo;
    private final Optional<KafkaProducer> kafkaProducer;

    public OrderService(OrderRepository orderRepo, ProductRepository productRepo, Optional<KafkaProducer> kafkaProducer) {
        this.orderRepo = orderRepo;
        this.productRepo = productRepo;
        this.kafkaProducer = kafkaProducer;
    }

    public OrderResponseDto createOrder(User user, OrderRequestDto dto) {
        // Validar que los productos existan
        List<Product> products = productRepo.findAllById(dto.getProductIds());
        
        if (products.size() != dto.getProductIds().size()) {
            throw new RuntimeException("Some products not found");
        }

        // Validar stock
        for (Product product : products) {
            if (product.getStock() <= 0) {
                throw new RuntimeException("Product " + product.getName() + " is out of stock");
            }
        }

        // Crear la orden
        Order order = new Order();
        order.setUser(user);
        order.setProducts(products);
        order.setCreatedAt(LocalDateTime.now());
        order.setTotal(calculateTotal(products));
        order.setStatus("PENDING");

        // Guardar la orden
        Order saved = orderRepo.save(order);

        // Enviar evento completo a Kafka (si está disponible)
        kafkaProducer.ifPresent(producer -> {
            OrderCreatedEvent event = new OrderCreatedEvent();
            event.setOrderId(saved.getId());
            event.setUserId(user.getId());
            event.setTotal(saved.getTotal());
            event.setTimestamp(System.currentTimeMillis());
            event.setStatus(saved.getStatus());

            producer.publishOrderCreated(event);
        });

        return mapToDto(saved);
    }

    public List<OrderResponseDto> getUserOrders(User user) {
        return orderRepo.findByUserId(user.getId()).stream()
            .map(this::mapToDto)
            .collect(Collectors.toList());
    }

    public List<OrderResponseDto> getAllOrders() {
        return orderRepo.findAll().stream()
            .map(this::mapToDto)
            .collect(Collectors.toList());
    }

    public OrderResponseDto updateOrderStatus(Long orderId, String status) {
        Order order = orderRepo.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));
        
        order.setStatus(status);
        Order updated = orderRepo.save(order);
        
        return mapToDto(updated);
    }

    public List<OrderResponseDto> getOrdersByUserId(Long userId) {
        return orderRepo.findByUserId(userId).stream()
            .map(this::mapToDto)
            .collect(Collectors.toList());
    }

    private Double calculateTotal(List<Product> products) {
        return products.stream()
            .mapToDouble(product -> product.getPrice().doubleValue())  // ← BigDecimal a double
            .sum();
    }

    private OrderResponseDto mapToDto(Order order) {
        List<ProductResponseDto> products = order.getProducts().stream().map(p -> {
            ProductResponseDto dto = new ProductResponseDto();
            dto.setId(p.getId());
            dto.setName(p.getName());
            dto.setDescription(p.getDescription());
            dto.setPrice(p.getPrice());
            dto.setStock(p.getStock());
            return dto;
        }).collect(Collectors.toList());

        OrderResponseDto responseDto = new OrderResponseDto();
        responseDto.setId(order.getId());
        responseDto.setUserId(order.getUser().getId());
        responseDto.setProducts(products);
        responseDto.setCreatedAt(order.getCreatedAt());
        responseDto.setStatus(order.getStatus());
        responseDto.setTotal(order.getTotal());
        return responseDto;
    }
}
