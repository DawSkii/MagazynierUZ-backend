package org.example.magazynieruz.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.example.magazynieruz.dto.location.CreateLocationRequest;
import org.example.magazynieruz.dto.location.LocationResponse;
import org.example.magazynieruz.dto.location.PatchLocationRequest;
import org.example.magazynieruz.mapper.LocationMapper;
import org.example.magazynieruz.model.Location;
import org.example.magazynieruz.service.LocationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/warehouses/{warehouseId}/locations")
@RequiredArgsConstructor
@Tag(name = "Locations", description = "Location management endpoints within warehouses")
@SecurityRequirement(name = "bearer-jwt")
public class LocationController {

    private final LocationService locationService;
    private final LocationMapper locationMapper;

    @GetMapping
    @Operation(summary = "Get all locations in a warehouse", description = "Retrieves all storage locations within a specific warehouse")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Locations retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - unauthorized"),
            @ApiResponse(responseCode = "404", description = "Warehouse not found")
    })
    public ResponseEntity<List<LocationResponse>> getLocations(
            @Parameter(description = "Warehouse ID", required = true) @PathVariable Long warehouseId) {
        List<Location> locations = locationService.getLocationsInWarehouse(warehouseId);

        List<LocationResponse> response = locations.stream()
                .map(locationMapper::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{locationId}")
    @Operation(summary = "Get location by ID", description = "Retrieves a single location by its ID within a warehouse")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Location retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - unauthorized"),
            @ApiResponse(responseCode = "404", description = "Location or warehouse not found")
    })
    public ResponseEntity<LocationResponse> getLocationById(
            @Parameter(description = "Warehouse ID", required = true) @PathVariable Long warehouseId,
            @Parameter(description = "Location ID", required = true) @PathVariable Long locationId) {
        LocationResponse location = locationService.getLocationById(warehouseId, locationId);
        return ResponseEntity.ok(location);
    }

    @PostMapping
    @Operation(summary = "Create a new location", description = "Creates a new storage location in the specified warehouse")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Location created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request - location code already exists"),
            @ApiResponse(responseCode = "403", description = "Access denied - unauthorized"),
            @ApiResponse(responseCode = "404", description = "Warehouse not found")
    })
    public ResponseEntity<LocationResponse> addLocation(
            @Parameter(description = "Warehouse ID", required = true) @PathVariable Long warehouseId,
            @Parameter(description = "Location details", required = true) @RequestBody CreateLocationRequest request) {

        Location created = locationService.createLocation(
                warehouseId,
                request.locationCode(),
                request.locationType(),
                request.zoneName()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(locationMapper.toResponse(created));
    }

    @PatchMapping("/{locationId}")
    @Operation(summary = "Partially update location", description = "Updates specific fields of a location. Only provided fields will be updated.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Location updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data or duplicate location code"),
            @ApiResponse(responseCode = "403", description = "Access denied - unauthorized"),
            @ApiResponse(responseCode = "404", description = "Location or warehouse not found")
    })
    public ResponseEntity<LocationResponse> updateLocation(
            @Parameter(description = "Warehouse ID", required = true) @PathVariable Long warehouseId,
            @Parameter(description = "Location ID", required = true) @PathVariable Long locationId,
            @Parameter(description = "Location update data", required = true) @Valid @RequestBody PatchLocationRequest request) {
        LocationResponse updated = locationService.updateLocation(warehouseId, locationId, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{locationId}")
    @Operation(summary = "Delete location", description = "Deletes a location by ID. Cannot delete if location has products.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Location deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Cannot delete - location has dependencies"),
            @ApiResponse(responseCode = "403", description = "Access denied - unauthorized"),
            @ApiResponse(responseCode = "404", description = "Location or warehouse not found")
    })
    public ResponseEntity<Void> deleteLocation(
            @Parameter(description = "Warehouse ID", required = true) @PathVariable Long warehouseId,
            @Parameter(description = "Location ID", required = true) @PathVariable Long locationId) {
        locationService.deleteLocation(warehouseId, locationId);
        return ResponseEntity.noContent().build();
    }
}
