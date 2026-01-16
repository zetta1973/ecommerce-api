package com.ecommerce.config;

import com.ecommerce.exception.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
    String field = ex.getBindingResult().getFieldError().getField();
    String message = ex.getBindingResult().getFieldError().getDefaultMessage();
    return ResponseEntity.badRequest().body(Map.of(field, message));
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<Map<String, String>> handleNotFound(ResourceNotFoundException ex) {
    return ResponseEntity.status(404).body(Map.of("error", ex.getMessage()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, String>> handleGeneral(Exception ex) {
    return ResponseEntity.internalServerError().body(Map.of("error", ex.getMessage()));
  }
}
