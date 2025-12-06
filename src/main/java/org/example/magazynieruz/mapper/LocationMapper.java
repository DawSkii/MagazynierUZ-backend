package org.example.magazynieruz.mapper;

import org.example.magazynieruz.dto.location.CreateLocationRequest;
import org.example.magazynieruz.dto.location.LocationResponse;
import org.example.magazynieruz.model.Location;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface LocationMapper {

    Location toEntity(CreateLocationRequest request);

    @Mapping(source = "active", target = "isActive")
    @Mapping(source = "locked", target = "isLocked")
    LocationResponse toResponse(Location location);
}
