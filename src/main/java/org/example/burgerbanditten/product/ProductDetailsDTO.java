package org.example.burgerbanditten.product;

import java.util.List;

public record ProductDetailsDTO(Long id,
                                String name,
                                String description,
                                Double price,
                                Boolean lunchOffer,
                                Size size,
                                Category category,
                                List<String> ingredients) {
}
