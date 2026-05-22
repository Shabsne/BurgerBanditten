package org.example.burgerbanditten.cart;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }


    @GetMapping("/{userId}")
    public ResponseEntity<Cart> getCart(@PathVariable Long userId) {
        return ResponseEntity.ok(cartService.getCartByUserId(userId));
    }

    @PostMapping("/{cartId}/add")
    public ResponseEntity<Cart> addToCart(@PathVariable Long cartId,
                                          @RequestBody AddToCartRequest request) {
        return ResponseEntity.ok(cartService.addProductToCart(
                cartId,
                request.getProductId(),
                request.getQuantity()));
    }


    @DeleteMapping("/item/{cartItemId}")
    public ResponseEntity<Void> removeItem(@PathVariable Long cartItemId) {
        cartService.removeOrReduceItem(cartItemId);
        return ResponseEntity.noContent().build();
    }

}