package org.example.burgerbanditten.product;

import org.example.burgerbanditten.ingredient.Ingredient;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductCardDTO toCardDTO(Product product) {
        return new ProductCardDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCategory(),
                product.getImage()
        );
    }

    // Bruges til getProduct og createProduct (Retunerer ProductDetailsDTO)
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
                        .map(Ingredient::getName) // ProductDetailsDTO forventer List<String> (navne)
                        .toList()
        );
    }

    // Bruges til updateProduct (Retunerer UpdateProductDTO)
    public UpdateProductDTO toUpdateDTO(Product product) {
        return new UpdateProductDTO(
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getLunchOffer(),
                product.getCategory(),
                product.getIngredients()
                        .stream()
                        .map(Ingredient::getId) // UpdateProductDTO forventer List<Long> (ID'er)
                        .toList(),
                product.getImage() // RETTET: Tilføjet det manglende 7. argument (String image)
        );
    }
}