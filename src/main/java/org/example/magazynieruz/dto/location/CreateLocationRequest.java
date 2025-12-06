package org.example.magazynieruz.dto.location;

import org.example.magazynieruz.model.LocationType;

public record CreateLocationRequest(
        String locationCode,
        String zoneName,
        LocationType locationType
) {}
