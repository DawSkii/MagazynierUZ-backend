package org.example.magazynieruz.dto.export;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Container for inventory export data with metadata")
public record InventoryExportData(
        @Schema(description = "Scope of the export", example = "WAREHOUSE")
        ExportScope scope,
        
        @Schema(description = "Export generation timestamp")
        LocalDateTime exportDate,
        
        @Schema(description = "Organisation name", example = "ABC Corporation")
        String organisationName,
        
        @Schema(description = "Warehouse name (if applicable)", example = "Main Warehouse")
        String warehouseName,
        
        @Schema(description = "Location name (if applicable)", example = "A1-01")
        String locationName,
        
        @Schema(description = "List of products with inventory data")
        List<ProductInventoryData> products
) {}
