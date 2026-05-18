package org.example.burgerbanditten.preorder.dto;

public record ValidationResponse(
        boolean valid,
        String message
) {
}
