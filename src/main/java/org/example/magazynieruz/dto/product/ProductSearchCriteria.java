package org.example.magazynieruz.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO for product search criteria")
public class ProductSearchCriteria {
    @Schema(description = "Search query for product name or description", example = "laptop")
    private String query;
    
    @Schema(description = "Filter by warehouse ID", example = "1")
    private Long warehouseId;
    
    @Schema(description = "Filter by location ID", example = "5")
    private Long locationId;
    
    @Schema(description = "Filter by organisation ID (automatically set from user context)", example = "1", hidden = true)
    private Long organisationId;
    
    @Schema(description = "Minimum price filter", example = "100.0")
    private Double minPrice;
    
    @Schema(description = "Maximum price filter", example = "5000.0")
    private Double maxPrice;
    
    @Schema(description = "Minimum quantity filter", example = "1")
    private Integer minQuantity;
    
    @Schema(description = "Maximum quantity filter", example = "500")
    private Integer maxQuantity;
    
    @Schema(description = "Filter only available products (quantity > 0)", example = "true")
    private Boolean isAvailable;
}
