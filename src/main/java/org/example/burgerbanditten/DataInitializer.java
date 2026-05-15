package org.example.burgerbanditten;

import org.example.burgerbanditten.Ingredient.Ingredient;
import org.example.burgerbanditten.Ingredient.IngredientRepository;
import org.example.burgerbanditten.user.Role;
import org.example.burgerbanditten.user.User;
import org.example.burgerbanditten.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.example.burgerbanditten.product.Category;
import org.example.burgerbanditten.product.Product;
import org.example.burgerbanditten.product.ProductRepository;

import java.util.List;

@Configuration
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private IngredientRepository ingredientRepository;

    @Autowired
    private UserRepository userRepository;


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
                Category.BURGER,
                List.of(cheese, bacon)
        );


        Product cola = new Product(
                null,
                "Coca Cola",
                "0.5L soda",
                25.0,
                false,
                Category.DRINK,
                null
        );

        productRepository.saveAll(
                List.of(
                        cheeseBurger,
                        cola
                )
        );

        User admin = new User(
                null,
                "Admin",
                "admin@mail.com",
                "admin123",
                Role.ADMIN
        );

        User customer = new User(
                null,
                "Customer",
                "Customer@mail.com",
                "customer123",
                Role.CUSTOMER
        );





        productRepository.save(cheeseBurger);


        userRepository.saveAll(
                List.of(
                        admin,
                        customer
                )
        );
    };

}