package org.example.burgerbanditten.order.dto;

import java.util.List;

public record SalesStatisticsDto(
        List<ProductSalesDto> productSales,
        double totalRevenue
) {
}
