package org.example.magazynieruz.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.magazynieruz.dto.location.CreateLocationRequest;
import org.example.magazynieruz.dto.location.LocationResponse;
import org.example.magazynieruz.dto.location.PatchLocationRequest;
import org.example.magazynieruz.mapper.LocationMapper;
import org.example.magazynieruz.model.Location;
import org.example.magazynieruz.model.LocationType;
import org.example.magazynieruz.model.Product;
import org.example.magazynieruz.model.Warehouse;
import org.example.magazynieruz.repository.LocationRepository;
import org.example.magazynieruz.repository.ProductRepository;
import org.example.magazynieruz.repository.WarehouseRepository;
import org.example.magazynieruz.security.UserContext;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for managing storage locations within warehouses.
 * Provides CRUD operations for locations with organisation-based access control.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LocationService {

    private final LocationRepository locationRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final LocationMapper locationMapper;
    private final UserContext userContext;

    /**
     * Retrieves all locations in a specific warehouse for the current user's organisation.
     *
     * @param warehouseId the warehouse ID
     * @return list of locations
     * @throws SecurityException if access denied
     */
    public List<Location> getLocationsInWarehouse(Long warehouseId) {
        verifyWarehouseAccess(warehouseId);

        return locationRepository.findByWarehouseId(warehouseId);
    }


    /**
     * Creates a new location in a warehouse.
     *
     * @param warehouseId the warehouse ID
     * @param code the location code
     * @param type the location type
     * @param zone the zone name
     * @return created location
     * @throws IllegalArgumentException if location code already exists
     * @throws SecurityException if access denied
     */
    @Transactional
    public Location createLocation(Long warehouseId, String code, LocationType type, String zone) {
        Warehouse warehouse = verifyWarehouseAccess(warehouseId);

        if (locationRepository.existsByLocationCodeAndWarehouseId(code, warehouseId)) {
            throw new IllegalArgumentException("Location " + code + " already exists");
        }

        Location location = Location.builder()
                .warehouse(warehouse)
                .locationCode(code)
                .locationType(type)
                .zoneName(zone)
                .isActive(true)
                .build();

        return locationRepository.save(location);
    }

    /**
     * Retrieves a specific location by ID.
     *
     * @param warehouseId the warehouse ID
     * @param locationId the location ID
     * @return location response
     * @throws IllegalArgumentException if location not found
     * @throws SecurityException if access denied
     */
    public LocationResponse getLocationById(Long warehouseId, Long locationId) {
        verifyWarehouseAccess(warehouseId);

        Location location = locationRepository.findByIdAndWarehouseId(locationId, warehouseId)
                .orElseThrow(() -> new IllegalArgumentException("Location not found"));

        return locationMapper.toResponse(location);
    }

    /**
     * Deletes a location and all associated products (cascade delete).
     *
     * @param warehouseId the warehouse ID
     * @param locationId the location ID
     * @throws IllegalArgumentException if location not found
     * @throws SecurityException if access denied
     */
    @Transactional
    public void deleteLocation(Long warehouseId, Long locationId) {
        verifyWarehouseAccess(warehouseId);

        Location location = locationRepository.findByIdAndWarehouseId(locationId, warehouseId)
                .orElseThrow(() -> new IllegalArgumentException("Location not found"));

        List<Product> products = productRepository.findByLocationId(locationId)
                .orElse(List.of());
        
        int productsDeleted = 0;
        if (!products.isEmpty()) {
            productRepository.deleteAll(products);
            productsDeleted = products.size();
            log.debug("Deleted {} products from location {} during cascade delete",
                     productsDeleted, location.getLocationCode());
        }

        locationRepository.delete(location);
        
        log.info("Cascade deleted location {} (ID: {}) from warehouse {}: {} products removed",
                location.getLocationCode(), locationId, warehouseId, productsDeleted);
    }

    /**
     * Updates a location with partial data.
     *
     * @param warehouseId the warehouse ID
     * @param locationId the location ID
     * @param request the update request
     * @return updated location response
     * @throws IllegalArgumentException if location not found or code already exists
     * @throws SecurityException if access denied
     */
    @Transactional
    public LocationResponse updateLocation(Long warehouseId, Long locationId, PatchLocationRequest request) {
        verifyWarehouseAccess(warehouseId);

        Location location = locationRepository.findByIdAndWarehouseId(locationId, warehouseId)
                .orElseThrow(() -> new IllegalArgumentException("Location not found"));

        request.locationCode().ifPresent(code -> {
            if (!code.equals(location.getLocationCode()) &&
                    locationRepository.existsByLocationCodeAndWarehouseId(code, warehouseId)) {
                throw new IllegalArgumentException("Location " + code + " already exists");
            }
            location.setLocationCode(code);
        });

        request.zoneName().ifPresent(location::setZoneName);
        request.locationType().ifPresent(location::setLocationType);
        request.isActive().ifPresent(location::setActive);
        request.isLocked().ifPresent(location::setLocked);

        Location savedLocation = locationRepository.save(location);
        return locationMapper.toResponse(savedLocation);
    }

    /**
     * Verifies warehouse access for the current user's organisation.
     *
     * @param warehouseId the warehouse ID
     * @return verified warehouse
     * @throws SecurityException if access denied
     */
    private Warehouse verifyWarehouseAccess(Long warehouseId) {
        Long orgId = userContext.getCurrentOrganisationId();

        return warehouseRepository.findByIdAndOrganisationId(warehouseId, orgId)
                .orElseThrow(() -> new SecurityException("Unauthorised"));
    }

    // ===== ADMIN METHODS WITH ORGANISATION OVERRIDE =====

    /**
     * Admin method: Retrieves all locations in a warehouse for a specific organisation.
     *
     * @param organisationId the organisation ID
     * @param warehouseId the warehouse ID
     * @return list of location responses
     * @throws EntityNotFoundException if warehouse not found
     */
    @Transactional
    public List<LocationResponse> getLocationsByWarehouseForOrganisation(Long organisationId, Long warehouseId) {
        Warehouse warehouse = warehouseRepository.findByIdAndOrganisationId(warehouseId, organisationId)
                .orElseThrow(() -> new EntityNotFoundException("Warehouse with id " + warehouseId + " not found for organisation " + organisationId));

        List<Location> locations = locationRepository.findByWarehouseId(warehouseId);
        return locations.stream()
                .map(locationMapper::toResponse)
                .toList();
    }

    /**
     * Admin method: Creates a new location in a warehouse for a specific organisation.
     *
     * @param organisationId the organisation ID
     * @param warehouseId the warehouse ID
     * @param request the creation request
     * @return created location response
     * @throws EntityNotFoundException if warehouse not found
     * @throws IllegalArgumentException if location code already exists
     */
    @Transactional
    public LocationResponse createLocationForWarehouse(Long organisationId, Long warehouseId, CreateLocationRequest request) {
        Warehouse warehouse = warehouseRepository.findByIdAndOrganisationId(warehouseId, organisationId)
                .orElseThrow(() -> new EntityNotFoundException("Warehouse with id " + warehouseId + " not found for organisation " + organisationId));

        if (locationRepository.existsByLocationCodeAndWarehouseId(request.locationCode(), warehouseId)) {
            throw new IllegalArgumentException("Location " + request.locationCode() + " already exists in this warehouse");
        }

        Location location = Location.builder()
                .warehouse(warehouse)
                .locationCode(request.locationCode())
                .locationType(request.locationType())
                .zoneName(request.zoneName())
                .isActive(true)
                .build();

        Location savedLocation = locationRepository.save(location);
        return locationMapper.toResponse(savedLocation);
    }

    /**
     * Admin method: Retrieves a specific location by ID for an organisation.
     *
     * @param organisationId the organisation ID
     * @param warehouseId the warehouse ID
     * @param locationId the location ID
     * @return location response
     * @throws EntityNotFoundException if warehouse or location not found
     */
    @Transactional
    public LocationResponse getLocationByIdForOrganisation(Long organisationId, Long warehouseId, Long locationId) {
        Warehouse warehouse = warehouseRepository.findByIdAndOrganisationId(warehouseId, organisationId)
                .orElseThrow(() -> new EntityNotFoundException("Warehouse with id " + warehouseId + " not found for organisation " + organisationId));

        Location location = locationRepository.findByIdAndWarehouseId(locationId, warehouseId)
                .orElseThrow(() -> new EntityNotFoundException("Location with id " + locationId + " not found in warehouse " + warehouseId));

        return locationMapper.toResponse(location);
    }

    /**
     * Admin method: Updates a location for a specific organisation.
     *
     * @param organisationId the organisation ID
     * @param warehouseId the warehouse ID
     * @param locationId the location ID
     * @param request the update request
     * @return updated location response
     * @throws EntityNotFoundException if warehouse or location not found
     * @throws IllegalArgumentException if location code already exists
     */
    @Transactional
    public LocationResponse updateLocationForOrganisation(Long organisationId, Long warehouseId, Long locationId, PatchLocationRequest request) {
        Warehouse warehouse = warehouseRepository.findByIdAndOrganisationId(warehouseId, organisationId)
                .orElseThrow(() -> new EntityNotFoundException("Warehouse with id " + warehouseId + " not found for organisation " + organisationId));

        Location location = locationRepository.findByIdAndWarehouseId(locationId, warehouseId)
                .orElseThrow(() -> new EntityNotFoundException("Location with id " + locationId + " not found in warehouse " + warehouseId));

        request.locationCode().ifPresent(code -> {
            if (!code.equals(location.getLocationCode()) &&
                    locationRepository.existsByLocationCodeAndWarehouseId(code, warehouseId)) {
                throw new IllegalArgumentException("Location " + code + " already exists in this warehouse");
            }
            location.setLocationCode(code);
        });

        request.zoneName().ifPresent(location::setZoneName);
        request.locationType().ifPresent(location::setLocationType);
        request.isActive().ifPresent(location::setActive);
        request.isLocked().ifPresent(location::setLocked);

        Location savedLocation = locationRepository.save(location);
        return locationMapper.toResponse(savedLocation);
    }

    /**
     * Admin method: Deletes a location for a specific organisation (cascade delete).
     *
     * @param organisationId the organisation ID
     * @param warehouseId the warehouse ID
     * @param locationId the location ID
     * @throws EntityNotFoundException if warehouse or location not found
     */
    @Transactional
    public void deleteLocationForOrganisation(Long organisationId, Long warehouseId, Long locationId) {
        Warehouse warehouse = warehouseRepository.findByIdAndOrganisationId(warehouseId, organisationId)
                .orElseThrow(() -> new EntityNotFoundException("Warehouse with id " + warehouseId + " not found for organisation " + organisationId));

        Location location = locationRepository.findByIdAndWarehouseId(locationId, warehouseId)
                .orElseThrow(() -> new EntityNotFoundException("Location with id " + locationId + " not found in warehouse " + warehouseId));

        List<Product> products = productRepository.findByLocationId(locationId)
                .orElse(List.of());

        int productsDeleted = 0;
        if (!products.isEmpty()) {
            productRepository.deleteAll(products);
            productsDeleted = products.size();
            log.debug("Deleted {} products from location {} during cascade delete",
                    productsDeleted, location.getLocationCode());
        }

        locationRepository.delete(location);

        log.info("Cascade deleted location {} (ID: {}) from warehouse {} for organisation {}: {} products removed",
                location.getLocationCode(), locationId, warehouseId, organisationId, productsDeleted);
    }
}
