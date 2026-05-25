package org.example.burgerbanditten.product.dto;

import org.example.burgerbanditten.product.Category;

import java.util.List;

public record UpdateProductDTO(
        String name,
        String description,
        Double price,
        Boolean lunchOffer,
        Category category,
        List<Long> ingredients,
        String image
) {
}
