package org.example.magazynieruz.controller;

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
public class WarehouseController {

    private final WarehouseService warehouseService;
    private final WarehouseMapper warehouseMapper;
    private final AddressMapper addressMapper;

    @GetMapping
    public ResponseEntity<List<WarehouseResponse>> getMyWarehouses() {
        List<Warehouse> warehouses = warehouseService.getMyWarehouses();

        List<WarehouseResponse> response = warehouses.stream()
                .map(warehouseMapper::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<WarehouseResponse> getWarehouse(@PathVariable Long id) {
        Warehouse warehouse = warehouseService.getWarehouse(id);
        return ResponseEntity.ok(warehouseMapper.toResponse(warehouse));
    }

    @PostMapping
    public ResponseEntity<WarehouseResponse> createWarehouse(@RequestBody CreateWarehouseRequest request) {

        Warehouse created = warehouseService.createWarehouse(request.name(), request.code(), addressMapper.toEntity(request.address()));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(warehouseMapper.toResponse(created));
    }
}
