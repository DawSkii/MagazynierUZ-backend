package org.example.magazynieruz.controller;

import lombok.RequiredArgsConstructor;
import org.example.magazynieruz.dto.location.CreateLocationRequest;
import org.example.magazynieruz.dto.location.LocationResponse;
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
public class LocationController {

    private final LocationService locationService;
    private final LocationMapper locationMapper;

    @GetMapping
    public ResponseEntity<List<LocationResponse>> getLocations(@PathVariable Long warehouseId) {
        List<Location> locations = locationService.getLocationsInWarehouse(warehouseId);

        List<LocationResponse> response = locations.stream()
                .map(locationMapper::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<LocationResponse> addLocation(
            @PathVariable Long warehouseId,
            @RequestBody CreateLocationRequest request) {

        Location created = locationService.createLocation(
                warehouseId,
                request.locationCode(),
                request.locationType(),
                request.zoneName()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(locationMapper.toResponse(created));
    }
}
