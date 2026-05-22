package org.example.burgerbanditten.ingredient;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ingredients")
public class IngredientController {

    private final IngredientService service;

    public IngredientController(IngredientService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Ingredient>> getIngredients() {
        return ResponseEntity.ok(service.getIngredients());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ingredient> getIngredient(@PathVariable Long id) {
        return ResponseEntity.ok(service.getIngredient(id));
    }


    @PutMapping("/{id}")
    public ResponseEntity<Ingredient> getIngredient(@PathVariable Long id, @RequestBody Ingredient updated) {
        return ResponseEntity.ok(service.updateIngredient(id, updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIngredient(@PathVariable Long id) {
        service.deleteIngredient(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<Ingredient> createIngredient(@RequestBody Ingredient ingredient) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createIngredient(ingredient));
    }
}