package org.example.magazynieruz.specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.example.magazynieruz.dto.product.ProductSearchCriteria;
import org.example.magazynieruz.model.Location;
import org.example.magazynieruz.model.Product;
import org.example.magazynieruz.model.Warehouse;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {
    public static Specification<Product> withCriteria(ProductSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.getQuery() != null && !criteria.getQuery().trim().isEmpty()) {
                String escapedQuery = criteria.getQuery().toLowerCase()
                        .replace("\\", "\\\\")
                        .replace("%", "\\%")
                        .replace("_", "\\_");

                String pattern = "%" + escapedQuery + "%";

                Predicate nameLike = cb.like(
                        cb.lower(root.get("name")),
                        pattern,
                        '\\'
                );

                Predicate descLike = cb.like(
                        cb.lower(cb.coalesce(root.get("description"), "")),
                        pattern,
                        '\\'
                );

                predicates.add(cb.or(nameLike, descLike));
            }

            Join<Product, Location> locationJoin = null;

            if (criteria.getLocationId() != null) {
                locationJoin = root.join("location");
                predicates.add(cb.equal(locationJoin.get("id"), criteria.getLocationId()));
            }

            if (criteria.getWarehouseId() != null || criteria.getOrganisationId() != null) {
                if (locationJoin == null) {
                    locationJoin = root.join("location");
                }
                Join<Location, Warehouse> warehouseJoin = locationJoin.join("warehouse");

                if (criteria.getWarehouseId() != null) {
                    predicates.add(cb.equal(warehouseJoin.get("id"), criteria.getWarehouseId()));
                }
                if (criteria.getOrganisationId() != null) {
                    predicates.add(cb.equal(warehouseJoin.get("organisation").get("id"), criteria.getOrganisationId()));
                }
            }

            if (criteria.getMinPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), criteria.getMinPrice()));
            }
            if (criteria.getMaxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), criteria.getMaxPrice()));
            }

            if (criteria.getMinQuantity() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("quantity"), criteria.getMinQuantity()));
            }
            if (criteria.getMaxQuantity() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("quantity"), criteria.getMaxQuantity()));
            }

            if (criteria.getIsAvailable() != null && criteria.getIsAvailable()) {
                predicates.add(cb.greaterThan(root.get("quantity"), 0));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}