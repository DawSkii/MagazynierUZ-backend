package org.example.magazynieruz.mapper;

import org.example.magazynieruz.dto.address.AddressResponse;
import org.example.magazynieruz.dto.address.CreateAddressRequest;
import org.example.magazynieruz.model.StructuredAddress;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface AddressMapper {

    StructuredAddress toEntity(CreateAddressRequest request);

    AddressResponse toResponse(StructuredAddress address);
}