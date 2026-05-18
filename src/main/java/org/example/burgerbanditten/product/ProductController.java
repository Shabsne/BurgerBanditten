package org.example.burgerbanditten.product;

import org.example.burgerbanditten.ingredient.Ingredient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
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

    @PostMapping("/admin/product/create")
    public ResponseEntity<ProductDetailsDTO> createProduct(@RequestBody CreateProductDTO dto) {
        return ResponseEntity.ok(service.createProduct(dto));
    }

    @PutMapping("/admin/product/update/{id}")
    public ResponseEntity<ProductDetailsDTO> updateProduct(@PathVariable Long id, @RequestBody UpdateProductDTO dto) {
        return ResponseEntity.ok(service.updateProduct(id, dto));
    }

    @DeleteMapping("/admin/product/delete/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        service.deleteProduct(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        List<String> categories = Arrays.stream(Category.values())
                .map(Enum::name)
                .toList();

        return ResponseEntity.ok(categories);
    }

    @GetMapping("/ingredients")
    public ResponseEntity<List<Ingredient>> getIngredients() {
        return ResponseEntity.ok(service.getIngredients());
    }


}
