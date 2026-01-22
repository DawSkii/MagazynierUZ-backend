package org.example.magazynieruz.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.magazynieruz.dto.location.LocationResponse;
import org.example.magazynieruz.dto.warehouse.WarehouseResponse;

@Schema(description = "Response containing product details")
public record ProductResponse(
        @Schema(description = "Unique identifier of the product", example = "1")
        Long id,

        @Schema(description = "Name of the product", example = "Laptop Dell XPS 15")
        String name,

        @Schema(description = "Detailed description of the product", example = "High-performance laptop with 16GB RAM")
        String description,

        @Schema(description = "Price of the product in the system currency", example = "4999.99")
        Double price,

        @Schema(description = "Available quantity of the product", example = "50")
        Integer quantity,

        @Schema(description = "Location where the product is stored")
        LocationResponse location,

        @Schema(description = "Warehouse where the product is located")
        WarehouseResponse warehouse
) {}
