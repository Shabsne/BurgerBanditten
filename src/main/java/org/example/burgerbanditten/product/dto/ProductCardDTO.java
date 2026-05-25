package org.example.burgerbanditten.product.dto;

import org.example.burgerbanditten.product.Category;

public record ProductCardDTO(Long id,
                             String name,
                             String description,
                             Double price,
                             Category category,
                             String image) {
}
