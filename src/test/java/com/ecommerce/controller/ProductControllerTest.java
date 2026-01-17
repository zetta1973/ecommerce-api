package com.ecommerce.controller;

import com.ecommerce.model.Product;
import com.ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProductControllerTest {

    private MockMvc mockMvc;
    private ProductController productController;
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        productRepository = Mockito.mock(ProductRepository.class);
        productController = new ProductController(productRepository);
        mockMvc = MockMvcBuilders.standaloneSetup(productController).build();
    }

    @Test
    void shouldListProducts() throws Exception {
        Product p1 = new Product(); p1.setId(1L);
        Product p2 = new Product(); p2.setId(2L);
        when(productRepository.findAll()).thenReturn(List.of(p1, p2));

        mockMvc.perform(get("/api/products"))
               .andExpect(status().isOk())
               .andExpect(content().json("[{},{}]")); // Solo comprueba que hay 2 elementos
    }

    @Test
    void shouldGetProductById() throws Exception {
        Product p = new Product(); p.setId(1L);
        when(productRepository.findById(1L)).thenReturn(Optional.of(p));

        mockMvc.perform(get("/api/products/1"))
               .andExpect(status().isOk());
    }

    @Test
    void shouldReturnNotFoundForMissingProduct() throws Exception {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/products/1"))
               .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateProduct() throws Exception {
        Product p = new Product(); p.setId(1L);
        when(productRepository.save(any(Product.class))).thenReturn(p);

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Test\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldUpdateProduct() throws Exception {
        Product p = new Product(); p.setId(1L);
        when(productRepository.existsById(1L)).thenReturn(true);
        when(productRepository.save(any(Product.class))).thenReturn(p);

        mockMvc.perform(put("/api/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Updated\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDeleteProduct() throws Exception {
        when(productRepository.existsById(1L)).thenReturn(true);
        doNothing().when(productRepository).deleteById(1L);

        mockMvc.perform(delete("/api/products/1"))
               .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnNotFoundWhenDeletingMissingProduct() throws Exception {
        when(productRepository.existsById(1L)).thenReturn(false);

        mockMvc.perform(delete("/api/products/1"))
               .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnProductStock() throws Exception {
        Product p = new Product(); p.setId(1L); p.setStock(10);
        when(productRepository.findById(1L)).thenReturn(Optional.of(p));

        mockMvc.perform(get("/api/products/1/stock"))
               .andExpect(status().isOk())
               .andExpect(content().string("10"));
    }

    @Test
    void shouldSearchProductsByName() throws Exception {
        Product p = new Product(); p.setId(1L);
        when(productRepository.findByNameContainingIgnoreCase("test")).thenReturn(List.of(p));

        mockMvc.perform(get("/api/products/search")
                .param("name", "test"))
                .andExpect(status().isOk());
    }
}
