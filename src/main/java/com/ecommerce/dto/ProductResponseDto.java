package com.ecommerce.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class ProductResponseDto {
  private Long id;
  private String name;
  private String description;
  private BigDecimal price;
  private int stock;
}
