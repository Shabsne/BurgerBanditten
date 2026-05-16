package org.example.burgerbanditten.order.dto;

import java.util.List;

public record OrderCreationRequest(
        List<OrderItemRequest> items,
        String customerEmail,
        String customerName,
        String comment,
        String pickUpDateTime
) {
}
