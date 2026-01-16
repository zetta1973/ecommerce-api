package com.ecommerce.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class OrderResponseDto {
    private Long id;
    private Long userId;
    private List<ProductResponseDto> products;
    private LocalDateTime createdAt;
    private String status;  // ← Añade este campo
    private Double total;   // ← Añade este campo
}
