package org.example.burgerbanditten.product.dto;

import org.example.burgerbanditten.product.Category;

import java.util.List;

public record ProductDetailsDTO(Long id,
                                String name,
                                String description,
                                Double price,
                                Boolean lunchOffer,
                                Category category,
                                List<String> ingredients) {
}
