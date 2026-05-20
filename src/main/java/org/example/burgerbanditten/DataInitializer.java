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
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(ProductRepository productRepository,
                           IngredientRepository ingredientRepository,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.productRepository = productRepository;
        this.ingredientRepository = ingredientRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        // ── Brugere ────────────────────────────────────────────
        // Opret kun brugere hvis der ingen er
        if (userRepository.count() == 0) {
            userRepository.save(new User(null, "Admin",    "admin@mail.com",
                    passwordEncoder.encode("admin123"),    Role.ADMIN));
            userRepository.save(new User(null, "Customer", "customer@mail.com",
                    passwordEncoder.encode("customer123"), Role.CUSTOMER));
        }

        // ── Produkter & ingredienser ───────────────────────────
        // Opret kun produkter hvis der ingen er.
        // FIX: den gamle check var på userRepository, så produkter
        // aldrig blev genskabt efter første kørsel.
        if (productRepository.count() > 0) return;

        // Ingredienser
        Ingredient cheese  = ingredientRepository.save(new Ingredient(null, "Cheddar ost",     5.0,  200, true));
        Ingredient bacon   = ingredientRepository.save(new Ingredient(null, "Bacon",           8.0,  150, true));
        Ingredient pickles = ingredientRepository.save(new Ingredient(null, "Pickles",         3.0,  300, true));
        Ingredient mayo    = ingredientRepository.save(new Ingredient(null, "Husets mayo",     4.0,  200, true));
        Ingredient onion   = ingredientRepository.save(new Ingredient(null, "Løg",            2.0,  200, true));
        Ingredient lettuce = ingredientRepository.save(new Ingredient(null, "Salat",          2.0,  200, true));
        Ingredient tomato  = ingredientRepository.save(new Ingredient(null, "Tomat",          2.0,  200, true));
        Ingredient chili   = ingredientRepository.save(new Ingredient(null, "Chilisauce",     3.0,  150, true));

        // ── Burgere ────────────────────────────────────────────
        productRepository.save(new Product(null,
                "Banditten",
                "Oksebøf, cheddar, pickles, løg, tomat, salat & husets mayo",
                99.0, false, Category.BURGER,
                List.of(cheese, pickles, onion, tomato, lettuce, mayo)));

        productRepository.save(new Product(null,
                "Cheese Burger",
                "Klassisk oksebøf med dobbelt cheddar og husets sauce",
                85.0, false, Category.BURGER,
                List.of(cheese, mayo, pickles)));

        productRepository.save(new Product(null,
                "Bandit Bacon",
                "Sprødt bacon, cheddar, chilisauce og løg på briochebolle",
                105.0, false, Category.BURGER,
                List.of(bacon, cheese, chili, onion)));

        productRepository.save(new Product(null,
                "Veggi'buggi",
                "Vegetarburger med salat, tomat, pickles og husets mayo",
                89.0, false, Category.BURGER,
                List.of(lettuce, tomato, pickles, mayo)));

        // ── Sides ──────────────────────────────────────────────
        productRepository.save(new Product(null,
                "Pommes Frites",
                "Sprøde klassiske fritter — saltet til perfektion",
                35.0, false, Category.SIDE,
                null));

        productRepository.save(new Product(null,
                "Bandit Tenderz (4 stk)",
                "Sprøde panerede kyllingestrimler med dip",
                45.0, false, Category.SIDE,
                null));

        // ── Drinks ─────────────────────────────────────────────
        productRepository.save(new Product(null,
                "Coca-Cola 0,5L",
                "Iskold sodavand",
                30.0, false, Category.DRINK,
                null));

        productRepository.save(new Product(null,
                "Vand 0,5L",
                "Kildevand",
                20.0, false, Category.DRINK,
                null));

        System.out.println("DataInitializer: Seed-data oprettet ✓");
    }
}