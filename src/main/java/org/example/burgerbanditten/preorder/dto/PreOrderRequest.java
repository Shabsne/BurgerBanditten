package org.example.burgerbanditten.preorder.dto;

import java.time.LocalDateTime;

public record PreOrderRequest(
        LocalDateTime pickUpDateTime,
        String customerEmail,
        String customerName
) {
}
