package org.example.magazynieruz.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.magazynieruz.model.Location;
import org.example.magazynieruz.model.LocationType;
import org.example.magazynieruz.model.Warehouse;
import org.example.magazynieruz.repository.LocationRepository;
import org.example.magazynieruz.repository.WarehouseRepository;
import org.example.magazynieruz.security.UserContext;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationRepository locationRepository;
    private final WarehouseRepository warehouseRepository;
    private final UserContext userContext;

    public List<Location> getLocationsInWarehouse(Long warehouseId) {
        verifyWarehouseAccess(warehouseId);

        return locationRepository.findByWarehouseId(warehouseId);
    }


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

    private Warehouse verifyWarehouseAccess(Long warehouseId) {
        Long orgId = userContext.getCurrentOrganisationId();

        return warehouseRepository.findByIdAndOrganisationId(warehouseId, orgId)
                .orElseThrow(() -> new SecurityException("Unauthorised"));
    }
}
