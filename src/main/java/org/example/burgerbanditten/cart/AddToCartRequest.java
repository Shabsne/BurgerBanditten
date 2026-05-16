package org.example.burgerbanditten.cart;

public class AddToCartRequest {

    private Long productId;
    private int quantity;

    public Long getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }
}