package org.example.burgerbanditten.product;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ProductController {

    private final ProductService service;


    public ProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping("/menu")
    public ResponseEntity<List<ProductCardDTO>> getProducts() {
        return ResponseEntity.ok(service.getProducts()
        );
    }

    @GetMapping("/product/{id}")
    public ResponseEntity<ProductDetailsDTO> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(service.getProduct(id));
    }
}
