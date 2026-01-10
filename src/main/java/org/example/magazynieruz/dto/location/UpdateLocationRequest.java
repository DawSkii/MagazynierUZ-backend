package org.example.magazynieruz.dto.location;

import org.example.magazynieruz.model.LocationType;

public record UpdateLocationRequest(
        String locationCode,
        String zoneName,
        LocationType locationType,
        Boolean isActive
) {}
