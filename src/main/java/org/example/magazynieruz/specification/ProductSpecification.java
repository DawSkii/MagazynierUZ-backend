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
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.getQuery() != null && !criteria.getQuery().trim().isEmpty()) {
                String searchPattern = "%" + criteria.getQuery().toLowerCase() + "%";
                Predicate namePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")), searchPattern);
                Predicate descriptionPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("description")), searchPattern);
                predicates.add(criteriaBuilder.or(namePredicate, descriptionPredicate));
            }

            if (criteria.getOrganisationId() != null) {
                Join<Product, Location> locationJoin = root.join("location");
                Join<Location, Warehouse> warehouseJoin = locationJoin.join("warehouse");
                predicates.add(criteriaBuilder.equal(warehouseJoin.get("organisation").get("id"), criteria.getOrganisationId()));
            }

            if (criteria.getWarehouseId() != null) {
                Join<Product, Location> locationJoin = root.join("location");
                Join<Location, Warehouse> warehouseJoin = locationJoin.join("warehouse");
                predicates.add(criteriaBuilder.equal(warehouseJoin.get("id"), criteria.getWarehouseId()));
            }

            if (criteria.getLocationId() != null) {
                Join<Product, Location> locationJoin = root.join("location");
                predicates.add(criteriaBuilder.equal(locationJoin.get("id"), criteria.getLocationId()));
            }

            if (criteria.getMinPrice() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), criteria.getMinPrice()));
            }
            if (criteria.getMaxPrice() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), criteria.getMaxPrice()));
            }

            if (criteria.getMinQuantity() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("quantity"), criteria.getMinQuantity()));
            }
            if (criteria.getMaxQuantity() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("quantity"), criteria.getMaxQuantity()));
            }

            if (criteria.getIsAvailable() != null && criteria.getIsAvailable()) {
                predicates.add(criteriaBuilder.greaterThan(root.get("quantity"), 0));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
