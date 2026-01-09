package org.example.magazynieruz.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request to create a new product within a location")
public record CreateProductRequest(
        @Schema(description = "Name of the product", example = "Laptop Dell XPS 15")
        String name,

        @Schema(description = "Detailed description of the product", example = "High-performance laptop with 16GB RAM")
        String description,

        @Schema(description = "Price of the product in the system currency", example = "4999.99")
        Double price,

        @Schema(description = "Available quantity of the product", example = "50")
        Integer quantity
) {}
