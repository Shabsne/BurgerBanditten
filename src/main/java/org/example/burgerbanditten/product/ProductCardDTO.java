package org.example.burgerbanditten.product;

public record ProductCardDTO(Long id,
                             String name,
                             String description,
                             Double price,
                             Category category) {
}
