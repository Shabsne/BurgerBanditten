package org.example.burgerbanditten.order.dto;

public record ProductSalesDto(
        Long productId,
        String productName,
        int quantitySold
) {
}
