package org.example.burgerbanditten.cart;

import org.example.burgerbanditten.product.Product;
import org.example.burgerbanditten.product.ProductRepository;
import org.springframework.stereotype.Service;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;

    public CartService(CartRepository cartRepository,
                       ProductRepository productRepository,
                       CartItemRepository cartItemRepository) {

        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.cartItemRepository = cartItemRepository;
    }

    public Cart addProductToCart(Long cartId, Long productId, int quantity) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        CartItem item = new CartItem();
        item.setCart(cart);
        item.setProduct(product);
        item.setQuantity(quantity);

        // Gem det nye item
        cartItemRepository.save(item);

        // Tilføj det til kurvens liste i hukommelsen (vigtigt for unit tests!)
        cart.getCartItems().add(item);

        return cart;
    }

    public void removeOrReduceItem(Long cartItemId) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("CartItem not found"));

        if (item.getQuantity() > 1) {
            // Acceptance criteria 2: reduce quantity if more than 1
            item.setQuantity(item.getQuantity() - 1);
            cartItemRepository.save(item);
        } else {
            // Acceptance criteria 1: remove entirely if quantity is 1
            cartItemRepository.delete(item);
        }
    }

    public Cart getCartByUserId(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
    }


    public double calculateTotal(Long userId) {
        Cart cart = getCartByUserId(userId);
        return cart.getCartItems().stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();
    }
}