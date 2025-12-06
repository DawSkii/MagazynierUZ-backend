package org.example.magazynieruz.repository;

import org.example.magazynieruz.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {

    List<Location> findByWarehouseId(Long warehouseId);

    Optional<Location> findByLocationCodeAndWarehouseId(String locationCode, Long warehouseId);

    boolean existsByLocationCodeAndWarehouseId(String locationCode, Long warehouseId);
}
