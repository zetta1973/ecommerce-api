package com.ecommerce.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private WebRequest request;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        request = mock(WebRequest.class);
    }

    @Test
    void shouldHandleGeneralException() {
        Exception ex = new Exception("General error");

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleGeneral(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsKey("error");
        assertThat(response.getBody().get("error")).isEqualTo("General error");
    }

    @Test
    void shouldHandleMethodArgumentNotValidException() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = mock(FieldError.class);

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldError()).thenReturn(fieldError);
        when(fieldError.getField()).thenReturn("email");
        when(fieldError.getDefaultMessage()).thenReturn("must not be empty");

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleValidation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isNotEmpty();
        assertThat(response.getBody()).containsKey("email");
        assertThat(response.getBody().get("email")).isEqualTo("must not be empty");
    }

    @Test
    void shouldHandleExceptionWithNullMessage() {
        Exception ex = new Exception("Test error");

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleGeneral(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsKey("error");
        assertThat(response.getBody().get("error")).isEqualTo("Test error");
    }

    @Test
    void shouldHandleExceptionWithEmptyMessage() {
        Exception ex = new Exception("Error message");

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleGeneral(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsKey("error");
        assertThat(response.getBody().get("error")).isEqualTo("Error message");
    }
}
