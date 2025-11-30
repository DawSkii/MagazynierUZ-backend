package org.example.magazynieruz.dto.product;

public record CreateProductRequest(
        String name,
        String description,
        Double price,
        Integer quantity
){}
