package org.example.magazynieruz.dto.product;

public record ProductResponse(
        Long id,
        String name,
        String description,
        Double price,
        Integer quantity
) {}
