package org.example.burgerbanditten.order.dto;

import java.util.List;

public record UpdateOrderRequest(

        String comment,
        String pickUpTime,
        List<UpdateOrderItem> items
) {
    public record UpdateOrderItem(
            Long productId,
            int quantity
    ) {}
}
