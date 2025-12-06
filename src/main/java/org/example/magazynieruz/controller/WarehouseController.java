package org.example.magazynieruz.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.magazynieruz.dto.warehouse.CreateWarehouseRequest;
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

@RestController
@RequestMapping("/api/v1/warehouses")
@RequiredArgsConstructor
@Tag(name = "Warehouses", description = "Warehouse management endpoints")
@SecurityRequirement(name = "bearer-jwt")
public class WarehouseController {

    private final WarehouseService warehouseService;
    private final WarehouseMapper warehouseMapper;
    private final AddressMapper addressMapper;

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
}
