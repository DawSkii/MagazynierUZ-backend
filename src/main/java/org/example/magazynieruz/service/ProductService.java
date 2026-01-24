package org.example.magazynieruz.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.magazynieruz.dto.product.CreateProductRequest;
import org.example.magazynieruz.dto.product.PatchProductRequest;
import org.example.magazynieruz.dto.product.ProductResponse;
import org.example.magazynieruz.dto.product.ProductSearchCriteria;
import org.example.magazynieruz.event.ProductQuantityChangedEvent;
import org.example.magazynieruz.mapper.ProductMapper;
import org.example.magazynieruz.model.Location;
import org.example.magazynieruz.model.Product;
import org.example.magazynieruz.model.Warehouse;
import org.example.magazynieruz.repository.LocationRepository;
import org.example.magazynieruz.repository.ProductRepository;
import org.example.magazynieruz.repository.WarehouseRepository;
import org.example.magazynieruz.specification.ProductSpecification;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * Service for managing products in warehouse locations.
 * Provides CRUD operations and search functionality for products with event publishing.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final LocationRepository locationRepository;
    private final WarehouseRepository warehouseRepository;

    /**
     * Creates a new product in a specified location.
     *
     * @param name the product name
     * @param quantity the initial quantity
     * @param description the product description
     * @param price the product price
     * @param location the storage location
     * @return created product
     * @throws IllegalArgumentException if product name already exists in location
     */
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

    /**
     * Retrieves a product by its ID.
     *
     * @param productId the product ID
     * @return product entity
     * @throws IllegalArgumentException if product not found
     */
    @Transactional
    public Product getProductById(Long productId){
        return productRepository.findByProductId(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product with id " + productId + " not found."));
    }

    /**
     * Retrieves all products in a specific location within a warehouse.
     *
     * @param warehouseId the warehouse ID
     * @param locationId the location ID
     * @return list of products
     * @throws IllegalArgumentException if no products found or warehouse mismatch
     */
    @Transactional
    public List<Product> getProductsByWarehouseIdAndLocationId(Long warehouseId,Long locationId){
        return productRepository.findByLocationId(locationId)
                .filter(products -> products.stream()
                        .allMatch(product -> product.getLocation().getWarehouse().getId().equals(warehouseId)))
                .orElseThrow(() -> new IllegalArgumentException("There are no products in location " + locationId + "."));
    }

    /**
     * Deletes a product from a specific location.
     *
     * @param warehouseId the warehouse ID
     * @param locationId the location ID
     * @param productId the product ID
     * @throws IllegalArgumentException if product not found or warehouse mismatch
     */
    @Transactional
    public void deleteProduct(Long warehouseId, Long locationId, Long productId) {
        Product product = productRepository.findByProductIdAndLocationId(productId, locationId)
                .orElseThrow(() -> new IllegalArgumentException("Product with id " + productId + " not found."));

        if (!product.getLocation().getWarehouse().getId().equals(warehouseId)) {
            throw new IllegalArgumentException("Product with id " + productId + " not found.");
        }

        productRepository.delete(product);
    }

    /**
     * Updates a product with partial data and publishes quantity change event if applicable.
     *
     * @param warehouseId the warehouse ID
     * @param locationId the location ID
     * @param productId the product ID
     * @param request the update request
     * @return updated product response
     * @throws IllegalArgumentException if product not found or name already exists
     */
    @Transactional
    public ProductResponse updateProduct(Long warehouseId, Long locationId, Long productId, PatchProductRequest request) {
        Product product = productRepository.findByProductIdAndLocationId(productId, locationId)
                .orElseThrow(() -> new IllegalArgumentException("Product with id " + productId + " not found."));

        if (!product.getLocation().getWarehouse().getId().equals(warehouseId)) {
            throw new IllegalArgumentException("Product with id " + productId + " not found.");
        }

        Integer oldQuantity = product.getQuantity();

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

        if (!Objects.equals(oldQuantity, savedProduct.getQuantity())) {
            log.debug("Product quantity changed for product ID {}: {} -> {}",
                    savedProduct.getProductId(), oldQuantity, savedProduct.getQuantity());
            publishQuantityChangedEvent(savedProduct, oldQuantity);
        }

        return productMapper.toResponse(savedProduct);
    }

    /**
     * Searches products based on criteria with pagination and sorting.
     *
     * @param criteria the search criteria
     * @param pageable pagination and sorting parameters
     * @return page of products matching criteria
     */
    @Transactional
    public Page<Product> searchProducts(ProductSearchCriteria criteria, Pageable pageable) {
        return productRepository.findAll(ProductSpecification.withCriteria(criteria), pageable);
    }

    /**
     * Publishes a product quantity changed event for monitoring and alerting.
     *
     * @param product the product entity
     * @param oldQuantity the previous quantity
     */
    private void publishQuantityChangedEvent(Product product, Integer oldQuantity) {
        try {
            Location location = product.getLocation();
            Warehouse warehouse = location != null ? location.getWarehouse() : null;
            Long organisationId = warehouse != null && warehouse.getOrganisation() != null
                    ? warehouse.getOrganisation().getId() : null;

            String locationName = location != null ? location.getLocationCode() : null;
            String warehouseName = warehouse != null ? warehouse.getWarehouseName() : null;

            ProductQuantityChangedEvent event = new ProductQuantityChangedEvent(
                    product.getProductId(),
                    product.getName(),
                    oldQuantity,
                    product.getQuantity(),
                    locationName,
                    warehouseName,
                    organisationId
            );

            eventPublisher.publishEvent(event);
            log.debug("Published ProductQuantityChangedEvent for product: {} (ID: {})",
                    product.getName(), product.getProductId());
        } catch (Exception e) {
            log.error("Failed to publish ProductQuantityChangedEvent for product ID {}: {}",
                    product.getProductId(), e.getMessage(), e);
        }
    }

    // ===== ADMIN METHODS WITH ORGANISATION OVERRIDE =====

    /**
     * Admin method: Retrieves all products in a location for a specific organisation.
     *
     * @param organisationId the organisation ID
     * @param warehouseId the warehouse ID
     * @param locationId the location ID
     * @return list of product responses
     * @throws EntityNotFoundException if warehouse or location not found
     */
    @Transactional
    public List<ProductResponse> getProductsByLocationForOrganisation(Long organisationId, Long warehouseId, Long locationId) {
        Warehouse warehouse = warehouseRepository.findByIdAndOrganisationId(warehouseId, organisationId)
                .orElseThrow(() -> new EntityNotFoundException("Warehouse with id " + warehouseId + " not found for organisation " + organisationId));

        Location location = locationRepository.findByIdAndWarehouseId(locationId, warehouseId)
                .orElseThrow(() -> new EntityNotFoundException("Location with id " + locationId + " not found in warehouse " + warehouseId));

        List<Product> products = productRepository.findByLocationId(locationId)
                .orElse(List.of());

        return products.stream()
                .map(productMapper::toResponse)
                .toList();
    }

    /**
     * Admin method: Creates a new product in a location for a specific organisation.
     *
     * @param organisationId the organisation ID
     * @param warehouseId the warehouse ID
     * @param locationId the location ID
     * @param request the creation request
     * @return created product response
     * @throws EntityNotFoundException if warehouse or location not found
     * @throws IllegalArgumentException if product name already exists
     */
    @Transactional
    public ProductResponse createProductForLocation(Long organisationId, Long warehouseId, Long locationId, CreateProductRequest request) {
        Warehouse warehouse = warehouseRepository.findByIdAndOrganisationId(warehouseId, organisationId)
                .orElseThrow(() -> new EntityNotFoundException("Warehouse with id " + warehouseId + " not found for organisation " + organisationId));

        Location location = locationRepository.findByIdAndWarehouseId(locationId, warehouseId)
                .orElseThrow(() -> new EntityNotFoundException("Location with id " + locationId + " not found in warehouse " + warehouseId));

        if (productRepository.existsByNameAndLocation(request.name(), location)) {
            throw new IllegalArgumentException("Product with name '" + request.name() + "' in location " + location.getLocationCode() + " already exists.");
        }

        Product product = Product.builder()
                .location(location)
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .quantity(request.quantity())
                .build();

        Product savedProduct = productRepository.save(product);
        return productMapper.toResponse(savedProduct);
    }

    /**
     * Admin method: Retrieves a specific product by ID for an organisation.
     *
     * @param organisationId the organisation ID
     * @param warehouseId the warehouse ID
     * @param locationId the location ID
     * @param productId the product ID
     * @return product response
     * @throws EntityNotFoundException if warehouse, location, or product not found
     */
    @Transactional
    public ProductResponse getProductByIdForOrganisation(Long organisationId, Long warehouseId, Long locationId, Long productId) {
        Warehouse warehouse = warehouseRepository.findByIdAndOrganisationId(warehouseId, organisationId)
                .orElseThrow(() -> new EntityNotFoundException("Warehouse with id " + warehouseId + " not found for organisation " + organisationId));

        Location location = locationRepository.findByIdAndWarehouseId(locationId, warehouseId)
                .orElseThrow(() -> new EntityNotFoundException("Location with id " + locationId + " not found in warehouse " + warehouseId));

        Product product = productRepository.findByProductIdAndLocationId(productId, locationId)
                .orElseThrow(() -> new EntityNotFoundException("Product with id " + productId + " not found in location " + locationId));

        return productMapper.toResponse(product);
    }

    /**
     * Admin method: Updates a product for a specific organisation.
     *
     * @param organisationId the organisation ID
     * @param warehouseId the warehouse ID
     * @param locationId the location ID
     * @param productId the product ID
     * @param request the update request
     * @return updated product response
     * @throws EntityNotFoundException if warehouse, location, or product not found
     * @throws IllegalArgumentException if product name already exists
     */
    @Transactional
    public ProductResponse updateProductForOrganisation(Long organisationId, Long warehouseId, Long locationId, Long productId, PatchProductRequest request) {
        Warehouse warehouse = warehouseRepository.findByIdAndOrganisationId(warehouseId, organisationId)
                .orElseThrow(() -> new EntityNotFoundException("Warehouse with id " + warehouseId + " not found for organisation " + organisationId));

        Location location = locationRepository.findByIdAndWarehouseId(locationId, warehouseId)
                .orElseThrow(() -> new EntityNotFoundException("Location with id " + locationId + " not found in warehouse " + warehouseId));

        Product product = productRepository.findByProductIdAndLocationId(productId, locationId)
                .orElseThrow(() -> new EntityNotFoundException("Product with id " + productId + " not found in location " + locationId));

        Integer oldQuantity = product.getQuantity();

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

        if (!Objects.equals(oldQuantity, savedProduct.getQuantity())) {
            log.debug("Product quantity changed for product ID {}: {} -> {}",
                    savedProduct.getProductId(), oldQuantity, savedProduct.getQuantity());
            publishQuantityChangedEvent(savedProduct, oldQuantity);
        }

        return productMapper.toResponse(savedProduct);
    }

    /**
     * Admin method: Deletes a product for a specific organisation.
     *
     * @param organisationId the organisation ID
     * @param warehouseId the warehouse ID
     * @param locationId the location ID
     * @param productId the product ID
     * @throws EntityNotFoundException if warehouse, location, or product not found
     */
    @Transactional
    public void deleteProductForOrganisation(Long organisationId, Long warehouseId, Long locationId, Long productId) {
        Warehouse warehouse = warehouseRepository.findByIdAndOrganisationId(warehouseId, organisationId)
                .orElseThrow(() -> new EntityNotFoundException("Warehouse with id " + warehouseId + " not found for organisation " + organisationId));

        Location location = locationRepository.findByIdAndWarehouseId(locationId, warehouseId)
                .orElseThrow(() -> new EntityNotFoundException("Location with id " + locationId + " not found in warehouse " + warehouseId));

        Product product = productRepository.findByProductIdAndLocationId(productId, locationId)
                .orElseThrow(() -> new EntityNotFoundException("Product with id " + productId + " not found in location " + locationId));

        productRepository.delete(product);
        log.info("Deleted product {} (ID: {}) from location {} for organisation {}",
                product.getName(), productId, locationId, organisationId);
    }

}
