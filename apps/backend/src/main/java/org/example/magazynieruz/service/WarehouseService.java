package org.example.magazynieruz.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.magazynieruz.model.Organisation;
import org.example.magazynieruz.model.StructuredAddress;
import org.example.magazynieruz.model.Warehouse;
import org.example.magazynieruz.repository.OrganisationRepository;
import org.example.magazynieruz.repository.WarehouseRepository;
import org.example.magazynieruz.security.UserContext;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final OrganisationRepository organisationRepository;
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
}