package org.example.magazynieruz.dto.location;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.magazynieruz.model.LocationType;

@Schema(description = "Response containing location details")
public record LocationResponse(
        @Schema(description = "Unique identifier of the location", example = "1")
        Long id,

        @Schema(description = "Unique code for the location within the warehouse", example = "A-01-01")
        String locationCode,

        @Schema(description = "Name of the zone where the location is situated", example = "Zone A")
        String zoneName,

        @Schema(description = "Type of location", example = "SHELF")
        LocationType locationType,

        @Schema(description = "Whether the location is active and can be used", example = "true")
        boolean isActive,

        @Schema(description = "Whether the location is locked for operations", example = "false")
        boolean isLocked
) {}
