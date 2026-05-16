package org.example.burgerbanditten.cart;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/{cartId}/add")
    public Cart addToCart(@PathVariable Long cartId,
                          @RequestBody AddToCartRequest request) {

        return cartService.addProductToCart(
                cartId,
                request.getProductId(),
                request.getQuantity()
        );
    }
}