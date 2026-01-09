package org.example.magazynieruz.dto.location;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import org.example.magazynieruz.model.LocationType;

import java.util.Optional;

@Schema(description = "Request DTO for partially updating a location")
public record PatchLocationRequest(
        @Schema(description = "Location code (unique within a warehouse)", example = "A-01-01")
        Optional<@Size(max = 50, message = "Location code must not exceed 50 characters") String> locationCode,

        @Schema(description = "Zone name for organizing locations", example = "Zone A")

        Optional<@Size(max = 50, message = "Zone name must not exceed 50 characters") String> zoneName,

        @Schema(description = "Type of location", example = "PICKING")
        Optional<LocationType> locationType,

        @Schema(description = "Whether the location is active", example = "true")
        Optional<Boolean> isActive,

        @Schema(description = "Whether the location is locked", example = "false")
        Optional<Boolean> isLocked
) {}
