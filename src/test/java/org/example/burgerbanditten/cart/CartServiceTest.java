package org.example.burgerbanditten.cart;

import org.example.burgerbanditten.product.Product;
import org.example.burgerbanditten.product.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CartServiceTest {

    private CartService cartService;
    private CartRepository cartRepository;
    private ProductRepository productRepository;
    private CartItemRepository cartItemRepository;

    @BeforeEach
    void setUp() {
        cartRepository = mock(CartRepository.class);
        productRepository = mock(ProductRepository.class);
        cartItemRepository = mock(CartItemRepository.class);

        cartService = new CartService(cartRepository, productRepository, cartItemRepository);
    }

    @Test
    void shouldAddProductToCart() {
        // --- ARRANGE ---
        Cart mockCart = new Cart();
        mockCart.setCartItems(new ArrayList<>());

        Product mockProduct = new Product();
        mockProduct.setName("Cheeseburger");

        when(cartRepository.findById(1L)).thenReturn(Optional.of(mockCart));
        when(productRepository.findById(10L)).thenReturn(Optional.of(mockProduct));

        // --- ACT ---
        cartService.addProductToCart(1L, 10L, 2);

        // --- ASSERT ---
        assertEquals(1, mockCart.getCartItems().size());

        // Nu er den ikke rød længere, fordi feltet er tilgængeligt i hele klassen
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }
}