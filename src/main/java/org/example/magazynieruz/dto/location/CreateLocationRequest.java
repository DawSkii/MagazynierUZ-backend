package org.example.magazynieruz.dto.location;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.magazynieruz.model.LocationType;

@Schema(description = "Request to create a new location within a warehouse")
public record CreateLocationRequest(
        @Schema(description = "Unique code for the location within the warehouse", example = "A-01-01")
        String locationCode,

        @Schema(description = "Name of the zone where the location is situated", example = "Zone A")
        String zoneName,

        @Schema(description = "Type of location (e.g., SHELF, PALLET, FLOOR)", example = "SHELF")
        LocationType locationType
) {}
