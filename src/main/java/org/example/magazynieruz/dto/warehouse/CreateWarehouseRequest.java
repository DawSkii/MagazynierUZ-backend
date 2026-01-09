package org.example.magazynieruz.dto.warehouse;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.magazynieruz.dto.address.CreateAddressRequest;

@Schema(description = "Request to create a new warehouse")
public record CreateWarehouseRequest(
        @Schema(description = "Name of the warehouse", example = "Main Distribution Center")
        String name,

        @Schema(description = "Unique code for the warehouse", example = "WH-001")
        String code,

        @Schema(description = "Detailed description of the warehouse", example = "Primary warehouse for electronic goods")
        String description,

        @Schema(description = "Physical address of the warehouse")
        CreateAddressRequest address
) {}
