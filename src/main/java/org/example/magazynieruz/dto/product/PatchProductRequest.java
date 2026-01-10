package org.example.magazynieruz.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.util.Optional;

@Schema(description = "Request DTO for partially updating a product")
public record PatchProductRequest(
        @Schema(description = "Product name", example = "Laptop")
        Optional<@Size(max = 255, message = "Product name must not exceed 255 characters") String> name,

        @Schema(description = "Product description", example = "High-performance laptop for business use")
        Optional<@Size(max = 1000, message = "Product description must not exceed 1000 characters") String> description,

        @Schema(description = "Product price", example = "1299.99")
        Optional<@DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0") Double> price,

        @Schema(description = "Product quantity in stock", example = "100")
        Optional<@Min(value = 0, message = "Quantity must be greater than or equal to 0") Integer> quantity
) {
}
