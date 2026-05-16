package org.example.burgerbanditten.order.dto;

import java.util.List;

public record OrderItemRequest(
        Long productId,
        int quantity,
        List<Long> selectedIngredients
) {
}
