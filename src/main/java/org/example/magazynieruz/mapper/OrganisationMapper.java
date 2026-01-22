package org.example.magazynieruz.mapper;

import org.example.magazynieruz.dto.organisation.CreateOrganisationRequest;
import org.example.magazynieruz.dto.organisation.OrganisationResponse;
import org.example.magazynieruz.model.Organisation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface OrganisationMapper {
    
    @Mapping(source = "tin", target = "TIN")
    Organisation toEntity(CreateOrganisationRequest request);

    @Mapping(source = "TIN", target = "tin")
    OrganisationResponse toResponse(Organisation organisation);
}
