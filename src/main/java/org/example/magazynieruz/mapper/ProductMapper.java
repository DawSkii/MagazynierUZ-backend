package org.example.magazynieruz.mapper;

import org.example.magazynieruz.dto.product.CreateProductRequest;
import org.example.magazynieruz.dto.product.ProductResponse;
import org.example.magazynieruz.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ProductMapper {
    Product toEntity(CreateProductRequest request);

    @Mapping(source = "productId", target = "id")
    ProductResponse toResponse(Product product);
}
