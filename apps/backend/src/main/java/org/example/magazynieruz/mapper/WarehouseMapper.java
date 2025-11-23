package org.example.magazynieruz.mapper;

import org.example.magazynieruz.dto.warehouse.CreateWarehouseRequest;
import org.example.magazynieruz.dto.warehouse.WarehouseResponse;
import org.example.magazynieruz.model.Warehouse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {AddressMapper.class}
)
public interface WarehouseMapper {

    @Mapping(source = "name", target = "warehouseName")
    @Mapping(source = "code", target = "warehouseCode")
    Warehouse toEntity(CreateWarehouseRequest request);

    @Mapping(source = "warehouseName", target = "name")
    @Mapping(source = "warehouseCode", target = "code")
    @Mapping(source = "active", target = "isActive")
    WarehouseResponse toResponse(Warehouse warehouse);
}
