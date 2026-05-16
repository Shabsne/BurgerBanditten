package org.example.burgerbanditten.product;

import org.example.burgerbanditten.ingredient.Ingredient;

import java.util.List;

public record CreateProductDTO(String name,
                               String description,
                               Double price,
                               Boolean lunchOffer,
                               Category category,
                               List<Long> ingredients,
                               String image) {
}
