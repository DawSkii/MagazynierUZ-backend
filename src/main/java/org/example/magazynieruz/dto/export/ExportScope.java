package org.example.magazynieruz.dto.export;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Scope level for inventory export")
public enum ExportScope {
    @Schema(description = "Export all products across all warehouses in the organisation")
    ORGANISATION,
    
    @Schema(description = "Export all products in a specific warehouse")
    WAREHOUSE,
    
    @Schema(description = "Export all products in a specific location")
    LOCATION
}
