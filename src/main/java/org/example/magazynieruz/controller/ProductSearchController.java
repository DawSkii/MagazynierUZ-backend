package org.example.magazynieruz.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.magazynieruz.dto.product.ProductResponse;
import org.example.magazynieruz.dto.product.ProductSearchCriteria;
import org.example.magazynieruz.mapper.ProductMapper;
import org.example.magazynieruz.model.Product;
import org.example.magazynieruz.security.UserContext;
import org.example.magazynieruz.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for advanced product search and filtering.
 * Provides endpoints for searching products with pagination, sorting, and filtering capabilities.
 */
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Product Search", description = "Advanced product search and filtering endpoints")
@SecurityRequirement(name = "bearer-jwt")
public class ProductSearchController {

    private final ProductService productService;
    private final ProductMapper productMapper;
    private final UserContext userContext;

    /**
     * Searches and filters products with pagination and sorting.
     *
     * @param query search query (searches in name and description)
     * @param warehouseId filter by warehouse ID
     * @param locationId filter by location ID
     * @param minPrice minimum price filter
     * @param maxPrice maximum price filter
     * @param minQuantity minimum quantity filter
     * @param maxQuantity maximum quantity filter
     * @param isAvailable filter only available products (quantity > 0)
     * @param page page number (0-indexed)
     * @param size page size
     * @param sortBy sort by field
     * @param sortDirection sort direction (asc or desc)
     * @return ResponseEntity containing paginated product results
     */
    @GetMapping("/search")
    @Operation(summary = "Search products", description = "Search and filter products with pagination and sorting")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Products retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid search parameters"),
            @ApiResponse(responseCode = "403", description = "Access denied - unauthorized")
    })
    public ResponseEntity<Page<ProductResponse>> searchProducts(
            @Parameter(description = "Search query (searches in name and description)")
            @RequestParam(required = false) String query,
            
            @Parameter(description = "Filter by warehouse ID")
            @RequestParam(required = false) Long warehouseId,
            
            @Parameter(description = "Filter by location ID")
            @RequestParam(required = false) Long locationId,
            
            @Parameter(description = "Minimum price")
            @RequestParam(required = false) Double minPrice,
            
            @Parameter(description = "Maximum price")
            @RequestParam(required = false) Double maxPrice,
            
            @Parameter(description = "Minimum quantity")
            @RequestParam(required = false) Integer minQuantity,
            
            @Parameter(description = "Maximum quantity")
            @RequestParam(required = false) Integer maxQuantity,
            
            @Parameter(description = "Filter only available products (quantity > 0)")
            @RequestParam(required = false) Boolean isAvailable,
            
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            
            @Parameter(description = "Sort by field (e.g., 'name', 'price', 'quantity')")
            @RequestParam(defaultValue = "name") String sortBy,
            
            @Parameter(description = "Sort direction (asc or desc)")
            @RequestParam(defaultValue = "asc") String sortDirection) {
        ProductSearchCriteria criteria = ProductSearchCriteria.builder()
                .query(query)
                .organisationId(userContext.getCurrentOrganisationId())
                .warehouseId(warehouseId)
                .locationId(locationId)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .minQuantity(minQuantity)
                .maxQuantity(maxQuantity)
                .isAvailable(isAvailable)
                .build();
        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<Product> productPage = productService.searchProducts(criteria, pageable);
        Page<ProductResponse> responsePage = productPage.map(productMapper::toResponse);
        return ResponseEntity.ok(responsePage);
    }

    /**
     * Retrieves top 10 products based on specified criteria and sorting.
     *
     * @param sortBy sort by field (e.g., 'quantity', 'price', 'name')
     * @param sortDirection sort direction (asc or desc)
     * @param warehouseId filter by warehouse ID
     * @param locationId filter by location ID
     * @param isAvailable filter only available products
     * @return ResponseEntity containing list of top 10 products
     */
    @GetMapping("/top10")
    @Operation(summary = "Get top 10 products", description = "Retrieve top 10 products based on specified criteria")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Top 10 products retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters"),
            @ApiResponse(responseCode = "403", description = "Access denied - unauthorized")
    })
    public ResponseEntity<List<ProductResponse>> getTop10Products(
            @Parameter(description = "Sort by field (e.g., 'quantity', 'price', 'name')")
            @RequestParam(defaultValue = "quantity") String sortBy,
            
            @Parameter(description = "Sort direction (asc or desc)")
            @RequestParam(defaultValue = "desc") String sortDirection,
            
            @Parameter(description = "Filter by warehouse ID")
            @RequestParam(required = false) Long warehouseId,
            
            @Parameter(description = "Filter by location ID")
            @RequestParam(required = false) Long locationId,
            
            @Parameter(description = "Filter only available products (quantity > 0)")
            @RequestParam(required = false, defaultValue = "true") Boolean isAvailable) {
        ProductSearchCriteria criteria = ProductSearchCriteria.builder()
                .organisationId(userContext.getCurrentOrganisationId())
                .warehouseId(warehouseId)
                .locationId(locationId)
                .isAvailable(isAvailable)
                .build();
        String normalizedSortBy = sortBy == null ? "" : sortBy.toLowerCase();
        List<String> allowedSortFields = List.of("quantity", "price", "name");
        if (!allowedSortFields.contains(normalizedSortBy)) {
            normalizedSortBy = "quantity";
        }
        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(0, 10, Sort.by(direction, normalizedSortBy));
        Page<Product> productPage = productService.searchProducts(criteria, pageable);
        List<ProductResponse> topProducts = productPage.getContent()
                .stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(topProducts);
    }
}
