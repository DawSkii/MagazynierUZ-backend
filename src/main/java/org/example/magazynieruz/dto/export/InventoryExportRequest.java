package org.example.magazynieruz.dto.export;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request to export inventory data at different scope levels")
public record InventoryExportRequest(
        @NotNull(message = "Export scope is required")
        @Schema(description = "Scope of the export (ORGANISATION, WAREHOUSE, or LOCATION)", 
                example = "WAREHOUSE", 
                required = true)
        ExportScope scope,
        
        @Schema(description = "Warehouse ID (required when scope is WAREHOUSE or LOCATION)", 
                example = "1")
        Long warehouseId,
        
        @Schema(description = "Location ID (required when scope is LOCATION)", 
                example = "5")
        Long locationId
) {}
