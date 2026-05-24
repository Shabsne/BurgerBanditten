package org.example.burgerbanditten.order.dto;

import java.util.List;

public record GuestOrderRequest(
        String customerName,
        String phone,
        String comment,
        String pickupDateTime,
        List<OrderItemRequest> items  // ← bruger nu den fælles DTO
) {}