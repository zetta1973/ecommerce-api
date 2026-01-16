package com.ecommerce.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserRegisterDto {
  @NotBlank
  private String username;

  @Email
  @NotBlank
  private String email;

  @NotBlank
  private String password;
}
