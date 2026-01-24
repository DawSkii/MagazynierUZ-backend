package org.example.magazynieruz.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.magazynieruz.dto.export.ExportScope;
import org.example.magazynieruz.dto.export.InventoryExportData;
import org.example.magazynieruz.dto.export.InventoryExportRequest;
import org.example.magazynieruz.dto.export.ProductInventoryData;
import org.example.magazynieruz.model.Location;
import org.example.magazynieruz.model.Organisation;
import org.example.magazynieruz.model.Product;
import org.example.magazynieruz.model.Warehouse;
import org.example.magazynieruz.repository.LocationRepository;
import org.example.magazynieruz.repository.OrganisationRepository;
import org.example.magazynieruz.repository.ProductRepository;
import org.example.magazynieruz.repository.WarehouseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for exporting inventory data.
 * Handles data retrieval and aggregation for inventory exports at different scopes.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryExportService {
    
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final LocationRepository locationRepository;
    private final OrganisationRepository organisationRepository;
    
    /**
     * Retrieves inventory data based on the specified scope and organisation.
     *
     * @param request the export request containing scope and filter criteria
     * @param organisationId the organisation ID
     * @return InventoryExportData containing aggregated inventory information
     * @throws IllegalArgumentException if request is invalid or resources not found
     */
    @Transactional(readOnly = true)
    public InventoryExportData getInventoryData(InventoryExportRequest request, Long organisationId) {
        log.info("Getting inventory data for scope: {} and organisationId: {}", request.scope(), organisationId);
        validateRequest(request);
        Organisation organisation = organisationRepository.findById(organisationId)
                .orElseThrow(() -> new IllegalArgumentException("Organisation not found with id: " + organisationId));
        List<ProductInventoryData> productData;
        String warehouseName = null;
        String locationName = null;
        switch (request.scope()) {
            case ORGANISATION -> {
                productData = getOrganisationInventory(organisationId);
            }
            case WAREHOUSE -> {
                Warehouse warehouse = warehouseRepository.findByIdAndOrganisationId(request.warehouseId(), organisationId)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Warehouse not found with id: " + request.warehouseId() +
                                " for organisation: " + organisationId));
                warehouseName = warehouse.getWarehouseName();
                productData = getWarehouseInventory(request.warehouseId());
            }
            case LOCATION -> {
                Location location = locationRepository.findById(request.locationId())
                        .orElseThrow(() -> new IllegalArgumentException("Location not found with id: " + request.locationId()));
                if (!location.getWarehouse().getId().equals(request.warehouseId())) {
                    throw new IllegalArgumentException(
                            "Location " + request.locationId() + " does not belong to warehouse " + request.warehouseId());
                }
                Warehouse warehouse = warehouseRepository.findByIdAndOrganisationId(request.warehouseId(), organisationId)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Warehouse not found with id: " + request.warehouseId() +
                                " for organisation: " + organisationId));
                warehouseName = warehouse.getWarehouseName();
                locationName = location.getLocationCode();
                productData = getLocationInventory(request.locationId());
            }
            default -> throw new IllegalArgumentException("Invalid export scope: " + request.scope());
        }
        log.info("Retrieved {} products for export", productData.size());
        return new InventoryExportData(
                request.scope(),
                LocalDateTime.now(),
                organisation.getName(),
                warehouseName,
                locationName,
                productData
        );
    }
    
    /**
     * Validates the export request parameters.
     *
     * @param request the request to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateRequest(InventoryExportRequest request) {
        if (request.scope() == null) {
            throw new IllegalArgumentException("Export scope is required");
        }
        
        if (request.scope() == ExportScope.WAREHOUSE && request.warehouseId() == null) {
            throw new IllegalArgumentException("Warehouse ID is required for WAREHOUSE scope");
        }
        
        if (request.scope() == ExportScope.LOCATION) {
            if (request.warehouseId() == null) {
                throw new IllegalArgumentException("Warehouse ID is required for LOCATION scope");
            }
            if (request.locationId() == null) {
                throw new IllegalArgumentException("Location ID is required for LOCATION scope");
            }
        }
    }
    
    /**
     * Retrieves inventory data for an entire organisation.
     *
     * @param organisationId the organisation ID
     * @return list of product inventory data
     */
    private List<ProductInventoryData> getOrganisationInventory(Long organisationId) {
        List<Warehouse> warehouses = warehouseRepository.findAllByOrganisationId(organisationId);
        List<ProductInventoryData> allProducts = new ArrayList<>();
        
        for (Warehouse warehouse : warehouses) {
            List<Location> locations = locationRepository.findByWarehouseId(warehouse.getId());
            for (Location location : locations) {
                List<Product> products = productRepository.findByLocationId(location.getId())
                        .orElse(List.of());
                allProducts.addAll(mapProductsToInventoryData(products, location, warehouse));
            }
        }
        
        return allProducts;
    }
    
    /**
     * Retrieves inventory data for a specific warehouse.
     *
     * @param warehouseId the warehouse ID
     * @return list of product inventory data
     */
    private List<ProductInventoryData> getWarehouseInventory(Long warehouseId) {
        List<Location> locations = locationRepository.findByWarehouseId(warehouseId);
        List<ProductInventoryData> allProducts = new ArrayList<>();
        
        for (Location location : locations) {
            Warehouse warehouse = location.getWarehouse();
            List<Product> products = productRepository.findByLocationId(location.getId())
                    .orElse(List.of());
            allProducts.addAll(mapProductsToInventoryData(products, location, warehouse));
        }
        
        return allProducts;
    }
    
    /**
     * Retrieves inventory data for a specific location.
     *
     * @param locationId the location ID
     * @return list of product inventory data
     */
    private List<ProductInventoryData> getLocationInventory(Long locationId) {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new IllegalArgumentException("Location not found with id: " + locationId));
        
        Warehouse warehouse = location.getWarehouse();
        List<Product> products = productRepository.findByLocationId(locationId)
                .orElse(List.of());
        
        return mapProductsToInventoryData(products, location, warehouse);
    }
    
    /**
     * Maps product entities to inventory data DTOs.
     *
     * @param products list of products
     * @param location the location entity
     * @param warehouse the warehouse entity
     * @return list of product inventory data
     */
    private List<ProductInventoryData> mapProductsToInventoryData(
            List<Product> products, Location location, Warehouse warehouse) {
        return products.stream()
                .map(product -> new ProductInventoryData(
                        product.getProductId(),
                        product.getName(),
                        product.getQuantity(),
                        location.getLocationCode(),
                        warehouse.getWarehouseName()
                ))
                .toList();
    }
}
