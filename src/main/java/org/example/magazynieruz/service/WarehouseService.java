package org.example.magazynieruz.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    public List<Warehouse> getMyWarehouses() {
        Long orgId = userContext.getCurrentOrganisationId();
        return warehouseRepository.findAllByOrganisationId(orgId);
    }

    public Warehouse getWarehouse(Long warehouseId) {
        Long orgId = userContext.getCurrentOrganisationId();
        return warehouseRepository.findByIdAndOrganisationId(warehouseId, orgId)
                .orElseThrow(() -> new EntityNotFoundException("Warehouse with id " + warehouseId + " for ogranisation id" + orgId + "not found"));
    }


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
}