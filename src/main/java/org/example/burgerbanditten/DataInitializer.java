package org.example.burgerbanditten;

import org.example.burgerbanditten.Ingredient.Ingredient;
import org.example.burgerbanditten.Ingredient.IngredientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.example.burgerbanditten.product.Category;
import org.example.burgerbanditten.product.Product;
import org.example.burgerbanditten.product.ProductRepository;
import org.example.burgerbanditten.product.Size;

import java.util.List;

@Configuration
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private IngredientRepository ingredientRepository;


    @Override
    public void run(String... args) throws Exception {

        Ingredient cheese = ingredientRepository.save(
                new Ingredient(
                        null,
                        "Cheese",
                        5.0,
                        100,
                        true
                )
        );

        Ingredient bacon = ingredientRepository.save(
                new Ingredient(
                        null,
                        "Bacon",
                        8.0,
                        100,
                        true
                )
        );

        Product cheeseBurger = new Product(
                null,
                "Cheese Burger",
                "Burger with cheese",
                79.0,
                true,
                Size.MEDIUM,
                Category.BURGER,
                List.of(cheese, bacon)
        );

        productRepository.save(cheeseBurger);

    };

}