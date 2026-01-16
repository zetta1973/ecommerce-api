package com.ecommerce.model;

import java.math.BigDecimal;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class Product {
  @Id @GeneratedValue
  private Long id;
  private String name;
  private String description;
  private BigDecimal price;
  private int stock;
}
