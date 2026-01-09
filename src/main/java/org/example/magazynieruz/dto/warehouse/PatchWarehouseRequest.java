package org.example.magazynieruz.dto.warehouse;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.example.magazynieruz.dto.address.CreateAddressRequest;

import java.util.Optional;

@Schema(description = "Request DTO for partially updating a warehouse")
public record PatchWarehouseRequest(
        @Schema(description = "Warehouse name", example = "Main Warehouse")
        @Size(max = 150, message = "Warehouse name must not exceed 150 characters")
        String name,

        @Schema(description = "Warehouse code (unique within an organisation)", example = "WH-001")
        @Size(max = 50, message = "Warehouse code must not exceed 50 characters")
        String code,

        @Schema(description = "Warehouse description", example = "Primary storage facility")
        Optional<@Size(max = 500, message = "Warehouse description must not exceed 500 characters") String> description,

        @Schema(description = "Whether the warehouse is active", example = "true")
        Boolean isActive,

        @Schema(description = "Warehouse address information")
        @Valid
        Optional<CreateAddressRequest> address
) {}
