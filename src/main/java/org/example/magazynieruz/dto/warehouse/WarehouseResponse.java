package org.example.magazynieruz.dto.warehouse;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.magazynieruz.dto.address.AddressResponse;

@Schema(description = "Response containing warehouse details")
public record WarehouseResponse(
        @Schema(description = "Unique identifier of the warehouse", example = "1")
        Long id,

        @Schema(description = "Name of the warehouse", example = "Main Distribution Center")
        String name,

        @Schema(description = "Unique code for the warehouse", example = "WH-001")
        String code,

        @Schema(description = "Whether the warehouse is currently active", example = "true")
        boolean isActive,

        @Schema(description = "Physical address of the warehouse")
        AddressResponse address
) {}