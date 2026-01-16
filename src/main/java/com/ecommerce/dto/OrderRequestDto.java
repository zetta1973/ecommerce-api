package com.ecommerce.dto;

import java.util.List;
import lombok.Data;

@Data
public class OrderRequestDto {
  private List<Long> productIds;
}
