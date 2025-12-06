package org.example.magazynieruz.dto.location;

import org.example.magazynieruz.model.LocationType;

public record LocationResponse(
        Long id,
        String locationCode,
        String zoneName,
        LocationType locationType,
        boolean isActive,
        boolean isLocked
) {}
