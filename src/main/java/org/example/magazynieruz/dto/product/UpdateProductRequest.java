package org.example.magazynieruz.dto.product;

public record UpdateProductRequest(
        String name,
        String description,
        Double price,
        Integer quantity
){}
