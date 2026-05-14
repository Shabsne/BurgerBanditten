package org.example.burgerbanditten.product;

import org.example.burgerbanditten.Ingredient.Ingredient;

import java.util.List;

public record ProductDetailsDTO(Long id,
                                String name,
                                String description,
                                Double price,
                                Boolean lunchOffer,
                                Category category,
                                List<String> ingredients) {
}
