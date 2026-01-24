package org.example.magazynieruz.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.magazynieruz.dto.warehouse.CreateWarehouseRequest;
import org.example.magazynieruz.dto.warehouse.PatchWarehouseRequest;
import org.example.magazynieruz.dto.warehouse.WarehouseResponse;
import org.example.magazynieruz.mapper.AddressMapper;
import org.example.magazynieruz.mapper.WarehouseMapper;
import org.example.magazynieruz.model.Warehouse;
import org.example.magazynieruz.service.WarehouseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for managing warehouses.
 * Provides CRUD operations for warehouses in the user's organisation.
 */
@RestController
@RequestMapping("/api/v1/warehouses")
@RequiredArgsConstructor
@Tag(name = "Warehouses", description = "Warehouse management endpoints")
@SecurityRequirement(name = "bearer-jwt")
public class WarehouseController {

    private final WarehouseService warehouseService;
    private final WarehouseMapper warehouseMapper;
    private final AddressMapper addressMapper;

    /**
     * Retrieves all warehouses belonging to the authenticated user's organization.
     *
     * @return ResponseEntity containing list of warehouses
     */
    @GetMapping
    @Operation(summary = "Get user's warehouses", description = "Retrieves all warehouses belonging to the authenticated user's organization")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Warehouses retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - unauthorized")
    })
    public ResponseEntity<List<WarehouseResponse>> getMyWarehouses() {
        List<Warehouse> warehouses = warehouseService.getMyWarehouses();
        List<WarehouseResponse> response = warehouses.stream()
                .map(warehouseMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a specific warehouse by its ID.
     *
     * @param id the warehouse ID
     * @return ResponseEntity containing the warehouse details
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get warehouse by ID", description = "Retrieves a specific warehouse by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Warehouse retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - unauthorized"),
            @ApiResponse(responseCode = "404", description = "Warehouse not found")
    })
    public ResponseEntity<WarehouseResponse> getWarehouse(
            @Parameter(description = "Warehouse ID", required = true) @PathVariable Long id) {
        Warehouse warehouse = warehouseService.getWarehouse(id);
        return ResponseEntity.ok(warehouseMapper.toResponse(warehouse));
    }

    /**
     * Creates a new warehouse for the authenticated user's organization.
     *
     * @param request the warehouse creation request
     * @return ResponseEntity containing the created warehouse details
     */
    @PostMapping
    @Operation(summary = "Create a new warehouse", description = "Creates a new warehouse for the authenticated user's organization")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Warehouse created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request - warehouse code already exists"),
            @ApiResponse(responseCode = "403", description = "Access denied - unauthorized")
    })
    public ResponseEntity<WarehouseResponse> createWarehouse(
            @Parameter(description = "Warehouse details", required = true) @RequestBody CreateWarehouseRequest request) {
        Warehouse created = warehouseService.createWarehouse(request.name(), request.code(), addressMapper.toEntity(request.address()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(warehouseMapper.toResponse(created));
    }

    /**
     * Partially updates a warehouse. Only provided fields will be updated.
     *
     * @param id the warehouse ID
     * @param request the warehouse update data
     * @return ResponseEntity containing the updated warehouse details
     */
    @PatchMapping("/{id}")
    @Operation(summary = "Partially update warehouse", description = "Updates specific fields of a warehouse. Only provided fields will be updated.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Warehouse updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data or duplicate warehouse code"),
            @ApiResponse(responseCode = "403", description = "Access denied - unauthorized"),
            @ApiResponse(responseCode = "404", description = "Warehouse not found")
    })
    public ResponseEntity<WarehouseResponse> updateWarehouse(
            @Parameter(description = "Warehouse ID", required = true) @PathVariable Long id,
            @Parameter(description = "Warehouse update data", required = true) @Valid @RequestBody PatchWarehouseRequest request) {
        WarehouseResponse updated = warehouseService.updateWarehouse(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * Deletes a warehouse by ID. Cannot delete if warehouse has locations.
     *
     * @param id the warehouse ID
     * @return ResponseEntity with no content
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete warehouse", description = "Deletes a warehouse by ID. Cannot delete if warehouse has locations.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Warehouse deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Cannot delete - warehouse has dependencies"),
            @ApiResponse(responseCode = "403", description = "Access denied - unauthorized"),
            @ApiResponse(responseCode = "404", description = "Warehouse not found")
    })
    public ResponseEntity<Void> deleteWarehouse(
            @Parameter(description = "Warehouse ID", required = true) @PathVariable Long id) {
        warehouseService.deleteWarehouse(id);
        return ResponseEntity.noContent().build();
    }
}
