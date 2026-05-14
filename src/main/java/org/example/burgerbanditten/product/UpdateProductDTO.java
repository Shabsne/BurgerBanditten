package org.example.burgerbanditten.product;

import java.util.List;

public record UpdateProductDTO(
        String name,
        String description,
        Double price,
        Boolean lunchOffer,
        Category category,
        List<Long> ingredients
) {
}
