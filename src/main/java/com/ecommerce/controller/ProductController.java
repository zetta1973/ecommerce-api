package com.ecommerce.controller;

import com.ecommerce.model.Product;
import com.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductRepository repo;

    // Lectura pública de productos
    @GetMapping
    public List<Product> getAllProducts() {
        return repo.findAll();
    }

    @PreAuthorize("hasAuthority('READ_PRODUCTS')")
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable Long id) {
        return repo.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority('CREATE_PRODUCTS')")
    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        Product savedProduct = repo.save(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
    }

    @PreAuthorize("hasAuthority('UPDATE_PRODUCTS')")
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        if (!repo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        product.setId(id);
        Product updatedProduct = repo.save(product);
        return ResponseEntity.ok(updatedProduct);
    }

    @PreAuthorize("hasAuthority('DELETE_PRODUCTS')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        if (!repo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('READ_PRODUCT_STOCK')")
    @GetMapping("/{id}/stock")
    public ResponseEntity<Integer> getProductStock(@PathVariable Long id) {
        return repo.findById(id)
            .map(product -> ResponseEntity.ok(product.getStock()))
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public List<Product> searchProducts(@RequestParam String name) {
        // Asegúrate de tener este método en tu repositorio
        return repo.findByNameContainingIgnoreCase(name);
    }
}
