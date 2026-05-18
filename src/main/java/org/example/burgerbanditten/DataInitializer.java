package org.example.burgerbanditten;

import org.example.burgerbanditten.ingredient.Ingredient;
import org.example.burgerbanditten.ingredient.IngredientRepository;
import org.example.burgerbanditten.user.Role;
import org.example.burgerbanditten.user.User;
import org.example.burgerbanditten.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.example.burgerbanditten.product.Category;
import org.example.burgerbanditten.product.Product;
import org.example.burgerbanditten.product.ProductRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public final class DataInitializer implements CommandLineRunner {


    private final ProductRepository productRepository;


    private final IngredientRepository ingredientRepository;


    private final UserRepository userRepository;

    // Injicér BCrypt encoder fra SecurityConfig

    private final PasswordEncoder passwordEncoder;

    public DataInitializer(ProductRepository productRepository, IngredientRepository ingredientRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.productRepository = productRepository;
        this.ingredientRepository = ingredientRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {

        // Opret kun data hvis databasen er tom
        if (userRepository.count() > 0) return;

        // ── Ingredienser ─────────────────────────────────
        Ingredient cheese = ingredientRepository.save(
                new Ingredient(null, "Cheese", 5.0, 100, true)
        );

        Ingredient bacon = ingredientRepository.save(
                new Ingredient(null, "Bacon", 8.0, 100, true)
        );

        // ── Produkter ────────────────────────────────────
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

        productRepository.saveAll(List.of(cheeseBurger, cola));

        // ── Brugere – adgangskoder hashes med BCrypt ─────
        User admin = new User(
                null,
                "Admin",
                "admin@mail.com",
                passwordEncoder.encode("admin123"),  // ← BCrypt hash
                Role.ADMIN
        );

        User customer = new User(
                null,
                "Customer",
                "customer@mail.com",
                passwordEncoder.encode("customer123"),  // ← BCrypt hash
                Role.CUSTOMER
        );

        userRepository.save(admin);
        userRepository.save(customer);
    }
}