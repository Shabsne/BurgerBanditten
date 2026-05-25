package org.example.burgerbanditten.order.dto;

import java.util.List;

public record UserOrderRequest(
        String comment,
        String pickUpTime,
        List<OrderItemRequest> items
) {}