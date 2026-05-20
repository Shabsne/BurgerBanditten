package org.example.burgerbanditten.order.dto;

import java.util.List;

public record GuestOrderRequest(
        String customerName,
        String phone,
        String comment,
        List<GuestOrderItem> items
) {
    // Nested record — matcher kurv-objektet fra localStorage
    public record GuestOrderItem(
            Long productId,
            String name,
            double price,
            int quantity
    ) {}
}