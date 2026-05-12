package org.example.burgerbanditten.cart;

import org.example.burgerbanditten.Email.EmailService;
import org.example.burgerbanditten.product.Product;
import org.example.burgerbanditten.product.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class CartServiceTest {

    @MockBean
    private EmailService emailService;

    @Autowired
    private CartService cartService;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void shouldAddProductToCart() {

        Cart cart = cartRepository.save(new Cart());

        Product product = new Product();
        product.setName("Burger");
        product.setPrice(59.0);
        product = productRepository.save(product);

        Cart updatedCart = cartService.addProductToCart(
                cart.getId(),
                product.getId(),
                2
        );

        assertEquals(1, updatedCart.getCartItems().size());
    }
}