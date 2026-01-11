package org.example.magazynieruz.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.magazynieruz.dto.product.PatchProductRequest;
import org.example.magazynieruz.dto.product.ProductResponse;
import org.example.magazynieruz.dto.product.ProductSearchCriteria;
import org.example.magazynieruz.mapper.ProductMapper;
import org.example.magazynieruz.model.Location;
import org.example.magazynieruz.model.Product;
import org.example.magazynieruz.repository.ProductRepository;
import org.example.magazynieruz.specification.ProductSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

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

    @Transactional
    public void deleteProduct(Long warehouseId, Long locationId, Long productId) {
        Product product = productRepository.findByProductIdAndLocationId(productId, locationId)
                .orElseThrow(() -> new IllegalArgumentException("Product with id " + productId + " not found."));

        if (!product.getLocation().getWarehouse().getId().equals(warehouseId)) {
            throw new IllegalArgumentException("Product with id " + productId + " not found.");
        }

        productRepository.delete(product);
    }

    @Transactional
    public ProductResponse updateProduct(Long warehouseId, Long locationId, Long productId, PatchProductRequest request) {
        Product product = productRepository.findByProductIdAndLocationId(productId, locationId)
                .orElseThrow(() -> new IllegalArgumentException("Product with id " + productId + " not found."));

        if (!product.getLocation().getWarehouse().getId().equals(warehouseId)) {
            throw new IllegalArgumentException("Product with id " + productId + " not found.");
        }

        request.name().ifPresent(name -> {
            if (!name.equals(product.getName()) &&
                    productRepository.existsByNameAndLocation(name, product.getLocation())) {
                throw new IllegalArgumentException("Product with name '" + name + "' in location " + product.getLocation().getLocationCode() + " already exists.");
            }
            product.setName(name);
        });

        request.description().ifPresent(product::setDescription);
        request.price().ifPresent(product::setPrice);
        request.quantity().ifPresent(product::setQuantity);

        Product savedProduct = productRepository.save(product);
        return productMapper.toResponse(savedProduct);
    }

    public Page<Product> searchProducts(ProductSearchCriteria criteria, Pageable pageable) {
        return productRepository.findAll(ProductSpecification.withCriteria(criteria), pageable);
    }

}
