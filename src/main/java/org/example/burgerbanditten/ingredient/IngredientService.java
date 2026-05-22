package org.example.burgerbanditten.ingredient;

import org.example.burgerbanditten.product.Product;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IngredientService {

    private final IngredientRepository repository;

    public IngredientService(IngredientRepository repository) {
        this.repository = repository;
    }

    public List<Ingredient> getIngredients() {
        return repository.findAll();
    }

    public Ingredient getIngredient(Long id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Ingrediens ikke fundet: " + id));
    }

    public Ingredient updateIngredient(Long id, Ingredient updated) {
        Ingredient ingredient = repository.findById(id).orElseThrow(() -> new RuntimeException("Ingrediens ikke fundet: " + id));

        ingredient.setName(updated.getName());
        ingredient.setPrice(updated.getPrice());
        ingredient.setInventory(updated.getInventory());
        ingredient.setAddOn(updated.isAddOn());

        return repository.save(ingredient);
    }

    public void deleteIngredient(Long id) {

        Ingredient ingredient = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ingrediens ikke fundet: " + id));

        for (Product product : ingredient.getProducts()) {
            product.getIngredients().remove(ingredient);
        }

        repository.delete(ingredient);
    }

    public Ingredient createIngredient(Ingredient ingredient) {
        return repository.save(ingredient);
    }
}
