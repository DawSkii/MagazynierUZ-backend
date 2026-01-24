package org.example.magazynieruz.repository;

import org.example.magazynieruz.model.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Warehouse entity data access.
 * Provides CRUD operations and custom queries for warehouse management.
 */
@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {

    List<Warehouse> findAllByOrganisationId(Long organisationId);

    Optional<Warehouse> findByIdAndOrganisationId(Long id, Long organisationId);

    boolean existsByWarehouseCodeAndOrganisationId(String warehouseCode, Long organisationId);
}