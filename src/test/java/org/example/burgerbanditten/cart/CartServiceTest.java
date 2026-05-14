package org.example.burgerbanditten.cart;

import org.example.burgerbanditten.Email.EmailService;
import org.example.burgerbanditten.product.Product;
import org.example.burgerbanditten.product.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Fortæller JUnit at vi bruger Mockito
public class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private CartService cartService; // Sprøjter automatisk de ovenstående @Mocks ind i servicen

    @Test
    void shouldAddProductToCart_WhenCartExists() {
        // Arrange
        Long cartId = 1L;
        Long productId = 10L;

        Cart cart = new Cart();
        cart.setId(cartId);
        cart.setCartItems(new ArrayList<>()); // Initialiser listen!

        Product product = new Product();
        product.setId(productId);
        product.setPrice(59.0);

        when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        // Act
        cartService.addProductToCart(cartId, productId, 2);

        // Assert
        // Vi verificerer, at det er CartItemRepository der gemmer, IKKE CartRepository
        verify(cartItemRepository, times(1)).save(any(CartItem.class));

        // Vi tjekker at varen faktisk blev tilføjet til listen i vores objekt
        assertEquals(1, cart.getCartItems().size());
    }

    @Test
    void shouldDeleteItem_WhenQuantityIsOne() {
        // Arrange
        Long itemId = 1L;
        CartItem item = new CartItem();
        item.setQuantity(1);

        when(cartItemRepository.findById(itemId)).thenReturn(Optional.of(item));

        // Act
        cartService.removeOrReduceItem(itemId);

        // Assert
        verify(cartItemRepository, times(1)).delete(item);
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void shouldReduceQuantity_WhenQuantityIsMoreThanOne() {
        // Arrange
        Long itemId = 1L;
        CartItem item = new CartItem();
        item.setQuantity(3);

        when(cartItemRepository.findById(itemId)).thenReturn(Optional.of(item));

        // Act
        cartService.removeOrReduceItem(itemId);

        // Assert
        assertEquals(2, item.getQuantity(), "Antallet skal trækkes fra med 1");
        verify(cartItemRepository, times(1)).save(item);
        verify(cartItemRepository, never()).delete(any());
    }
}