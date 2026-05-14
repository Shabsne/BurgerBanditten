package org.example.burgerbanditten.product;

import org.example.burgerbanditten.Ingredient.Ingredient;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductCardDTO toCardDTO(Product product) {

        return new ProductCardDTO(product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCategory()
        );
    }

    public ProductDetailsDTO toDetailsDTO(Product product) {
        return new ProductDetailsDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getLunchOffer(),
                product.getCategory(),
                product.getIngredients()
                        .stream()
                        .map(Ingredient::getName)
                        .toList()
        );
    }
}
