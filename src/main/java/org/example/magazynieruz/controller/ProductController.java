package org.example.magazynieruz.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.magazynieruz.dto.product.CreateProductRequest;
import org.example.magazynieruz.dto.product.PatchProductRequest;
import org.example.magazynieruz.dto.product.ProductResponse;
import org.example.magazynieruz.mapper.ProductMapper;
import org.example.magazynieruz.model.Location;
import org.example.magazynieruz.model.Product;
import org.example.magazynieruz.service.LocationService;
import org.example.magazynieruz.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/warehouses/{warehouseId}/{locationId}/products")
@Tag(name = "Products", description = "Product management endpoints")
@SecurityRequirement(name = "bearer-jwt")
public class ProductController {
    private final ProductService productService;
    private final ProductMapper productMapper;
    private final LocationService locationService;

    @GetMapping
    @Operation(summary = "Get all products in a location", description = "Retrieves all products stored in a specific location within a warehouse")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Products retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - unauthorized"),
            @ApiResponse(responseCode = "404", description = "Warehouse or location not found")
    })
    public ResponseEntity<List<ProductResponse>> getProducts(
            @Parameter(description = "Warehouse ID", required = true) @PathVariable Long warehouseId,
            @Parameter(description = "Location ID", required = true) @PathVariable Long locationId) {
        List<Product> products = productService.getProductsByWarehouseIdAndLocationId(warehouseId, locationId);

        List<ProductResponse> response = products.stream()
                .map(productMapper::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{productId}")
    @Operation(summary = "Get product by ID", description = "Retrieves a specific product stored in a given location within a warehouse by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - unauthorized"),
            @ApiResponse(responseCode = "404", description = "Warehouse, location, or product not found")
    })
    public ResponseEntity<ProductResponse> getProducts(
            @Parameter(description = "Warehouse ID", required = true) @PathVariable Long warehouseId,
            @Parameter(description = "Location ID", required = true) @PathVariable Long locationId,
            @Parameter(description = "Product ID", required = true) @PathVariable Long productId) {
        Product product = productService.getProductById(productId);

        return ResponseEntity.ok(productMapper.toResponse(product));
    }

    @PostMapping
    @Operation(summary = "Add a new product", description = "Creates a new product in the specified location")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request - product already exists"),
            @ApiResponse(responseCode = "403", description = "Access denied - unauthorized"),
            @ApiResponse(responseCode = "404", description = "Warehouse or location not found")
    })
    public ResponseEntity<ProductResponse> addProduct(
            @Parameter(description = "Product details", required = true) @RequestBody CreateProductRequest request,
            @Parameter(description = "Warehouse ID", required = true) @PathVariable Long warehouseId,
            @Parameter(description = "Location ID", required = true) @PathVariable Long locationId) {
        
        Location location = locationService.getLocationsInWarehouse(warehouseId).stream()
                .filter(loc -> loc.getId().equals(locationId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Location with id " + locationId + " not found in warehouse " + warehouseId));
        
        Product product = productService.createProduct(
                request.name(),
                request.quantity(),
                request.description(),
                request.price(),
                location
        );

        return ResponseEntity.ok(productMapper.toResponse(product));
    }

    @PatchMapping("/{productId}")
    @Operation(summary = "Partially update product", description = "Updates specific fields of a product. Only provided fields will be updated.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data or duplicate product name"),
            @ApiResponse(responseCode = "403", description = "Access denied - unauthorized"),
            @ApiResponse(responseCode = "404", description = "Warehouse, location, or product not found")
    })
    public ResponseEntity<ProductResponse> updateProduct(
            @Parameter(description = "Warehouse ID", required = true) @PathVariable Long warehouseId,
            @Parameter(description = "Location ID", required = true) @PathVariable Long locationId,
            @Parameter(description = "Product ID", required = true) @PathVariable Long productId,
            @Parameter(description = "Product update data", required = true) @Valid @RequestBody PatchProductRequest request) {
        ProductResponse updated = productService.updateProduct(warehouseId, locationId, productId, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{productId}")
    @Operation(summary = "Delete product", description = "Deletes a product by ID from a specific location")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Product deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - unauthorized"),
            @ApiResponse(responseCode = "404", description = "Warehouse, location, or product not found")
    })
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "Warehouse ID", required = true) @PathVariable Long warehouseId,
            @Parameter(description = "Location ID", required = true) @PathVariable Long locationId,
            @Parameter(description = "Product ID", required = true) @PathVariable Long productId) {
        productService.deleteProduct(warehouseId, locationId, productId);
        return ResponseEntity.noContent().build();
    }
}
