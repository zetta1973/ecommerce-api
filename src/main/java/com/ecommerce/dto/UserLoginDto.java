package com.ecommerce.dto;

import jakarta.validation.constraints.Email; 
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserLoginDto {
  @NotBlank
  @Email
  private String email;
  private String password;

  // Getters and setters
}
