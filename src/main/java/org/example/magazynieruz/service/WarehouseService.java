package org.example.magazynieruz.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.magazynieruz.dto.warehouse.CreateWarehouseRequest;
import org.example.magazynieruz.dto.warehouse.PatchWarehouseRequest;
import org.example.magazynieruz.dto.warehouse.WarehouseResponse;
import org.example.magazynieruz.mapper.AddressMapper;
import org.example.magazynieruz.mapper.WarehouseMapper;
import org.example.magazynieruz.model.Location;
import org.example.magazynieruz.model.Organisation;
import org.example.magazynieruz.model.Product;
import org.example.magazynieruz.model.StructuredAddress;
import org.example.magazynieruz.model.Warehouse;
import org.example.magazynieruz.repository.LocationRepository;
import org.example.magazynieruz.repository.OrganisationRepository;
import org.example.magazynieruz.repository.ProductRepository;
import org.example.magazynieruz.repository.WarehouseRepository;
import org.example.magazynieruz.security.UserContext;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for managing warehouses.
 * Provides CRUD operations for warehouses with organisation-based access control.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final OrganisationRepository organisationRepository;
    private final LocationRepository locationRepository;
    private final ProductRepository productRepository;
    private final WarehouseMapper warehouseMapper;
    private final AddressMapper addressMapper;
    private final UserContext userContext;

    /**
     * Retrieves all warehouses for the current user's organisation.
     *
     * @return list of warehouses
     */
    public List<Warehouse> getMyWarehouses() {
        Long orgId = userContext.getCurrentOrganisationId();
        return warehouseRepository.findAllByOrganisationId(orgId);
    }

    /**
     * Retrieves a specific warehouse by ID for the current user's organisation.
     *
     * @param warehouseId the warehouse ID
     * @return warehouse entity
     * @throws EntityNotFoundException if warehouse not found
     */
    public Warehouse getWarehouse(Long warehouseId) {
        Long orgId = userContext.getCurrentOrganisationId();
        return warehouseRepository.findByIdAndOrganisationId(warehouseId, orgId)
                .orElseThrow(() -> new EntityNotFoundException("Warehouse with id " + warehouseId + " for ogranisation id" + orgId + "not found"));
    }


    /**
     * Creates a new warehouse for the current user's organisation.
     *
     * @param name the warehouse name
     * @param code the warehouse code
     * @param address the warehouse address
     * @return created warehouse
     * @throws IllegalArgumentException if warehouse code already exists
     */
    @Transactional
    public Warehouse createWarehouse(String name, String code, StructuredAddress address) {
        Long orgId = userContext.getCurrentOrganisationId();

        if (warehouseRepository.existsByWarehouseCodeAndOrganisationId(code, orgId)) {
            throw new IllegalArgumentException("Warehouse" + code + " already exists.");
        }

        Organisation org = organisationRepository.getReferenceById(orgId);

        Warehouse warehouse = Warehouse.builder()
                .warehouseName(name)
                .warehouseCode(code)
                .organisation(org)
                .address(address)
                .isActive(true)
                .build();

        return warehouseRepository.save(warehouse);
    }

    /**
     * Deletes a warehouse and all associated locations and products (cascade delete).
     *
     * @param warehouseId the warehouse ID
     */
    @Transactional
    public void deleteWarehouse(Long warehouseId) {
        Warehouse warehouse = getWarehouse(warehouseId);
        
        List<Location> locations = locationRepository.findByWarehouseId(warehouseId);
        
        int totalProductsDeleted = 0;
        int totalLocationsDeleted = 0;
        
        for (Location location : locations) {
            List<Product> products = productRepository.findByLocationId(location.getId())
                    .orElse(List.of());
            
            if (!products.isEmpty()) {
                productRepository.deleteAll(products);
                totalProductsDeleted += products.size();
                log.debug("Deleted {} products from location {} during warehouse cascade delete",
                         products.size(), location.getLocationCode());
            }
        }
        
        if (!locations.isEmpty()) {
            locationRepository.deleteAll(locations);
            totalLocationsDeleted = locations.size();
            log.debug("Deleted {} locations during warehouse cascade delete", totalLocationsDeleted);
        }
        
        warehouseRepository.delete(warehouse);
        
        log.info("Cascade deleted warehouse {} (ID: {}): {} locations and {} products removed",
                warehouse.getWarehouseCode(), warehouseId, totalLocationsDeleted, totalProductsDeleted);
    }

    /**
     * Updates a warehouse with partial data.
     *
     * @param warehouseId the warehouse ID
     * @param request the update request
     * @return updated warehouse response
     * @throws IllegalArgumentException if warehouse code already exists
     */
    @Transactional
    public WarehouseResponse updateWarehouse(Long warehouseId, PatchWarehouseRequest request) {
        Warehouse warehouse = getWarehouse(warehouseId);
        Long orgId = userContext.getCurrentOrganisationId();

        if (request.name() != null) {
            warehouse.setWarehouseName(request.name());
        }

        if (request.code() != null) {
            if (!request.code().equals(warehouse.getWarehouseCode()) &&
                    warehouseRepository.existsByWarehouseCodeAndOrganisationId(request.code(), orgId)) {
                throw new IllegalArgumentException("Warehouse " + request.code() + " already exists.");
            }
            warehouse.setWarehouseCode(request.code());
        }

        request.description().ifPresent(warehouse::setDescription);

        if (request.isActive() != null) {
            warehouse.setActive(request.isActive());
        }

        request.address().ifPresent(addressRequest -> {
            StructuredAddress address = addressMapper.toEntity(addressRequest);
            warehouse.setAddress(address);
        });

        Warehouse savedWarehouse = warehouseRepository.save(warehouse);
        return warehouseMapper.toResponse(savedWarehouse);
    }

    // ===== ADMIN METHODS WITH ORGANISATION OVERRIDE =====

    /**
     * Admin method: Retrieves all warehouses for a specific organisation.
     *
     * @param organisationId the organisation ID
     * @return list of warehouse responses
     */
    @Transactional
    public List<WarehouseResponse> getWarehousesByOrganisationId(Long organisationId) {
        List<Warehouse> warehouses = warehouseRepository.findAllByOrganisationId(organisationId);
        return warehouses.stream()
                .map(warehouseMapper::toResponse)
                .toList();
    }

    /**
     * Admin method: Creates a new warehouse for a specific organisation.
     *
     * @param organisationId the organisation ID
     * @param request the creation request
     * @return created warehouse response
     * @throws EntityNotFoundException if organisation not found
     * @throws IllegalArgumentException if warehouse code already exists
     */
    @Transactional
    public WarehouseResponse createWarehouseForOrganisation(Long organisationId, CreateWarehouseRequest request) {
        Organisation organisation = organisationRepository.findById(organisationId)
                .orElseThrow(() -> new EntityNotFoundException("Organisation with id " + organisationId + " not found"));

        if (warehouseRepository.existsByWarehouseCodeAndOrganisationId(request.code(), organisationId)) {
            throw new IllegalArgumentException("Warehouse " + request.code() + " already exists in this organisation.");
        }

        StructuredAddress address = request.address() != null ? addressMapper.toEntity(request.address()) : null;

        Warehouse warehouse = Warehouse.builder()
                .warehouseName(request.name())
                .warehouseCode(request.code())
                .description(request.description())
                .organisation(organisation)
                .address(address)
                .isActive(true)
                .build();

        Warehouse savedWarehouse = warehouseRepository.save(warehouse);
        return warehouseMapper.toResponse(savedWarehouse);
    }

    /**
     * Admin method: Retrieves a specific warehouse by ID for an organisation.
     *
     * @param organisationId the organisation ID
     * @param warehouseId the warehouse ID
     * @return warehouse response
     * @throws EntityNotFoundException if warehouse not found
     */
    @Transactional
    public WarehouseResponse getWarehouseByIdForOrganisation(Long organisationId, Long warehouseId) {
        Warehouse warehouse = warehouseRepository.findByIdAndOrganisationId(warehouseId, organisationId)
                .orElseThrow(() -> new EntityNotFoundException("Warehouse with id " + warehouseId + " not found for organisation " + organisationId));
        return warehouseMapper.toResponse(warehouse);
    }

    /**
     * Admin method: Updates a warehouse for a specific organisation.
     *
     * @param organisationId the organisation ID
     * @param warehouseId the warehouse ID
     * @param request the update request
     * @return updated warehouse response
     * @throws EntityNotFoundException if warehouse not found
     * @throws IllegalArgumentException if warehouse code already exists
     */
    @Transactional
    public WarehouseResponse updateWarehouseForOrganisation(Long organisationId, Long warehouseId, PatchWarehouseRequest request) {
        Warehouse warehouse = warehouseRepository.findByIdAndOrganisationId(warehouseId, organisationId)
                .orElseThrow(() -> new EntityNotFoundException("Warehouse with id " + warehouseId + " not found for organisation " + organisationId));

        if (request.name() != null) {
            warehouse.setWarehouseName(request.name());
        }

        if (request.code() != null) {
            if (!request.code().equals(warehouse.getWarehouseCode()) &&
                    warehouseRepository.existsByWarehouseCodeAndOrganisationId(request.code(), organisationId)) {
                throw new IllegalArgumentException("Warehouse " + request.code() + " already exists in this organisation.");
            }
            warehouse.setWarehouseCode(request.code());
        }

        request.description().ifPresent(warehouse::setDescription);

        if (request.isActive() != null) {
            warehouse.setActive(request.isActive());
        }

        request.address().ifPresent(addressRequest -> {
            StructuredAddress address = addressMapper.toEntity(addressRequest);
            warehouse.setAddress(address);
        });

        Warehouse savedWarehouse = warehouseRepository.save(warehouse);
        return warehouseMapper.toResponse(savedWarehouse);
    }

    /**
     * Admin method: Deletes a warehouse for a specific organisation (cascade delete).
     *
     * @param organisationId the organisation ID
     * @param warehouseId the warehouse ID
     * @throws EntityNotFoundException if warehouse not found
     */
    @Transactional
    public void deleteWarehouseForOrganisation(Long organisationId, Long warehouseId) {
        Warehouse warehouse = warehouseRepository.findByIdAndOrganisationId(warehouseId, organisationId)
                .orElseThrow(() -> new EntityNotFoundException("Warehouse with id " + warehouseId + " not found for organisation " + organisationId));

        List<Location> locations = locationRepository.findByWarehouseId(warehouseId);

        int totalProductsDeleted = 0;
        int totalLocationsDeleted = 0;

        for (Location location : locations) {
            List<Product> products = productRepository.findByLocationId(location.getId())
                    .orElse(List.of());

            if (!products.isEmpty()) {
                productRepository.deleteAll(products);
                totalProductsDeleted += products.size();
                log.debug("Deleted {} products from location {} during warehouse cascade delete",
                        products.size(), location.getLocationCode());
            }
        }

        if (!locations.isEmpty()) {
            locationRepository.deleteAll(locations);
            totalLocationsDeleted = locations.size();
            log.debug("Deleted {} locations during warehouse cascade delete", totalLocationsDeleted);
        }

        warehouseRepository.delete(warehouse);

        log.info("Cascade deleted warehouse {} (ID: {}) for organisation {}: {} locations and {} products removed",
                warehouse.getWarehouseCode(), warehouseId, organisationId, totalLocationsDeleted, totalProductsDeleted);
    }
}