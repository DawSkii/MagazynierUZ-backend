package org.example.magazynieruz.repository;

import org.example.magazynieruz.model.Location;
import org.example.magazynieruz.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByProductId(Long productId);
    Optional<Product> findByName(String name);
    Optional<List<Product>> findByLocationId(Long locationId);
    boolean existsByNameAndLocation(String name, Location location);
}
