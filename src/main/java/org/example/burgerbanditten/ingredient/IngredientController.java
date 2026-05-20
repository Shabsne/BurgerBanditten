package org.example.burgerbanditten.ingredient;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/ingredients")
public class IngredientController {

    private final IngredientRepository ingredientRepository;

    public IngredientController(IngredientRepository ingredientRepository) {
        this.ingredientRepository = ingredientRepository;
    }

    // GET alle ingredienser — bruges i admin ingrediens-liste
    @GetMapping
    public ResponseEntity<List<Ingredient>> getAll() {
        return ResponseEntity.ok(ingredientRepository.findAll());
    }

    // POST opret ny ingrediens
    @PostMapping
    public ResponseEntity<Ingredient> create(@RequestBody Ingredient ingredient) {
        ingredient.setId(null); // Undgå at overskrive eksisterende
        Ingredient saved = ingredientRepository.save(ingredient);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // PUT opdater ingrediens
    @PutMapping("/{id}")
    public ResponseEntity<Ingredient> update(@PathVariable Long id,
                                             @RequestBody Ingredient ingredient) {
        if (!ingredientRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        ingredient.setId(id);
        return ResponseEntity.ok(ingredientRepository.save(ingredient));
    }

    // DELETE slet ingrediens
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!ingredientRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        ingredientRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}