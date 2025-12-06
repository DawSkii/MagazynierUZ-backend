package org.example.magazynieruz.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.magazynieruz.model.Location;
import org.example.magazynieruz.model.Product;
import org.example.magazynieruz.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional
    public Product createProduct(String name, Integer quantity, String description, Double price, Location location) {

        if(productRepository.existsByNameAndLocation(name, location)){
            throw new IllegalArgumentException("Product with name '" + name + "' in location " + location.getLocationCode() + " already exists.");
        }

        Product product = Product.builder()
            .location(location)
            .name(name)
            .description(description)
            .price(price)
            .quantity(quantity)
            .build();

        return productRepository.save(product);
    }

    public Product getProductById(Long productId){
        return productRepository.findByProductId(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product with id " + productId + " not found."));
    }

    public List<Product> getProductsByWarehouseIdAndLocationId(Long warehouseId,Long locationId){

        return productRepository.findByLocationId(locationId)
                .filter(products -> products.stream()
                        .allMatch(product -> product.getLocation().getWarehouse().getId().equals(warehouseId)))
                .orElseThrow(() -> new IllegalArgumentException("There are no products in location " + locationId + "."));
    }

}
