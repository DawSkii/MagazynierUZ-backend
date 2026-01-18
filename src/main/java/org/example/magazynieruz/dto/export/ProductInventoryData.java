package org.example.magazynieruz.dto.export;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Product inventory data for export")
public record ProductInventoryData(
        @Schema(description = "Product ID", example = "1")
        Long productId,
        
        @Schema(description = "Product name", example = "Laptop Dell XPS 15")
        String productName,
        
        @Schema(description = "Quantity available", example = "50")
        Integer quantity,
        
        @Schema(description = "Location name", example = "A1-01")
        String locationName,
        
        @Schema(description = "Warehouse name", example = "Main Warehouse")
        String warehouseName
) {}
