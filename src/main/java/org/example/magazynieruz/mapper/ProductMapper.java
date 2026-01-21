package org.example.magazynieruz.mapper;

import org.example.magazynieruz.dto.product.CreateProductRequest;
import org.example.magazynieruz.dto.product.ProductResponse;
import org.example.magazynieruz.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {LocationMapper.class, WarehouseMapper.class}
)
public interface ProductMapper {
    Product toEntity(CreateProductRequest request);

    @Mapping(source = "productId", target = "id")
    @Mapping(source = "location", target = "location")
    @Mapping(source = "location.warehouse", target = "warehouse")
    ProductResponse toResponse(Product product);
}
