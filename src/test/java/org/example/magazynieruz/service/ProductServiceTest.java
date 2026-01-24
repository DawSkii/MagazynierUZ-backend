package org.example.magazynieruz.service;

import org.example.magazynieruz.dto.location.LocationResponse;
import org.example.magazynieruz.dto.product.PatchProductRequest;
import org.example.magazynieruz.dto.product.ProductResponse;
import org.example.magazynieruz.dto.product.ProductSearchCriteria;
import org.example.magazynieruz.dto.warehouse.WarehouseResponse;
import org.example.magazynieruz.event.ProductQuantityChangedEvent;
import org.example.magazynieruz.mapper.ProductMapper;
import org.example.magazynieruz.model.Location;
import org.example.magazynieruz.model.LocationType;
import org.example.magazynieruz.model.Organisation;
import org.example.magazynieruz.model.Product;
import org.example.magazynieruz.model.Warehouse;
import org.example.magazynieruz.repository.LocationRepository;
import org.example.magazynieruz.repository.ProductRepository;
import org.example.magazynieruz.repository.WarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link ProductService}.
 * Verifies product CRUD operations, search functionality, and event publishing.
 */
@SpringBootTest
@Transactional
@RecordApplicationEvents
class ProductServiceTest {

    @MockitoBean
    private ProductRepository productRepository;

    @MockitoBean
    private ProductMapper productMapper;

    @MockitoBean
    private LocationRepository locationRepository;

    @MockitoBean
    private WarehouseRepository warehouseRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private ApplicationEvents applicationEvents;

    private Location testLocation;
    private Warehouse testWarehouse;
    private Organisation testOrganisation;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        reset(productRepository, productMapper, locationRepository, warehouseRepository);
        
        testOrganisation = new Organisation();
        testOrganisation.setId(1L);
        testOrganisation.setName("Test Org");

        testWarehouse = new Warehouse();
        testWarehouse.setId(1L);
        testWarehouse.setWarehouseName("Main Warehouse");
        testWarehouse.setOrganisation(testOrganisation);

        testLocation = new Location();
        testLocation.setId(1L);
        testLocation.setLocationCode("A1-01");
        testLocation.setWarehouse(testWarehouse);

        testProduct = Product.builder()
                .productId(1L)
                .name("Laptop")
                .description("Gaming laptop")
                .price(1500.0)
                .quantity(100)
                .location(testLocation)
                .build();
    }

    /**
     * Tests product creation with valid data and expects successful creation.
     */
    @Test
    void testCreateProduct_Success() {
        when(productRepository.existsByNameAndLocation("Laptop", testLocation)).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        Product result = productService.createProduct("Laptop", 100, "Gaming laptop", 1500.0, testLocation);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Laptop");
        
        verify(productRepository).save(any(Product.class));
    }

    /**
     * Tests product creation with duplicate name in same location and expects IllegalArgumentException.
     */
    @Test
    void testCreateProduct_DuplicateName_ThrowsException() {
        when(productRepository.existsByNameAndLocation("Laptop", testLocation)).thenReturn(true);

        assertThatThrownBy(() -> productService.createProduct("Laptop", 100, "Gaming laptop", 1500.0, testLocation))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
        
        verify(productRepository, never()).save(any(Product.class));
    }

    /**
     * Tests retrieving product by ID with valid ID and expects product details.
     */
    @Test
    void testGetProductById_Success() {
        when(productRepository.findByProductId(1L)).thenReturn(Optional.of(testProduct));

        Product result = productService.getProductById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getProductId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Laptop");
    }

    /**
     * Tests retrieving product by non-existent ID and expects IllegalArgumentException.
     */
    @Test
    void testGetProductById_NotFound_ThrowsException() {
        when(productRepository.findByProductId(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }

    /**
     * Tests retrieving products by warehouse and location IDs and expects product list.
     */
    @Test
    void testGetProductsByWarehouseIdAndLocationId_Success() {
        List<Product> products = Arrays.asList(testProduct);
        when(productRepository.findByLocationId(1L)).thenReturn(Optional.of(products));

        List<Product> result = productService.getProductsByWarehouseIdAndLocationId(1L, 1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Laptop");
    }

    /**
     * Tests deleting product and expects successful deletion.
     */
    @Test
    void testDeleteProduct_Success() {
        when(productRepository.findByProductIdAndLocationId(1L, 1L)).thenReturn(Optional.of(testProduct));

        productService.deleteProduct(1L, 1L, 1L);

        verify(productRepository).delete(testProduct);
    }

    /**
     * Tests deleting non-existent product and expects IllegalArgumentException.
     */
    @Test
    void testDeleteProduct_NotFound_ThrowsException() {
        when(productRepository.findByProductIdAndLocationId(999L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.deleteProduct(1L, 1L, 999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }

    /**
     * Tests updating product with valid data and expects updated product response.
     */
    @Test
    void testUpdateProduct_Success() {
        PatchProductRequest request = new PatchProductRequest(
                Optional.of("Updated Laptop"),
                Optional.of("Updated description"),
                Optional.of(1600.0),
                Optional.of(90)
        );

        LocationResponse locationResponse = new LocationResponse(1L, "A1-01", "Zone A", LocationType.PICKING, true, false);
        WarehouseResponse warehouseResponse = new WarehouseResponse(1L, "Main Warehouse", "WH-001", true, null);
        ProductResponse expectedResponse = new ProductResponse(
                1L, "Updated Laptop", "Updated description", 1600.0, 90, locationResponse, warehouseResponse
        );

        when(productRepository.findByProductIdAndLocationId(1L, 1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.existsByNameAndLocation(anyString(), any(Location.class))).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        when(productMapper.toResponse(any(Product.class))).thenReturn(expectedResponse);

        ProductResponse result = productService.updateProduct(1L, 1L, 1L, request);

        assertThat(result).isNotNull();
        verify(productRepository).save(any(Product.class));
    }

    /**
     * Tests that updating product quantity publishes ProductQuantityChangedEvent with correct details.
     */
    @Test
    void testUpdateProduct_QuantityChanged_PublishesEvent() {
        PatchProductRequest request = new PatchProductRequest(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(50)
        );

        LocationResponse locationResponse = new LocationResponse(1L, "A1-01", "Zone A", LocationType.PICKING, true, false);
        WarehouseResponse warehouseResponse = new WarehouseResponse(1L, "Main Warehouse", "WH-001", true, null);
        ProductResponse expectedResponse = new ProductResponse(
                1L, "Laptop", "Gaming laptop", 1500.0, 50, locationResponse, warehouseResponse
        );

        when(productRepository.findByProductIdAndLocationId(1L, 1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        when(productMapper.toResponse(any(Product.class))).thenReturn(expectedResponse);

        productService.updateProduct(1L, 1L, 1L, request);

        long eventCount = applicationEvents.stream(ProductQuantityChangedEvent.class).count();
        assertThat(eventCount).isEqualTo(1);
        
        ProductQuantityChangedEvent event = applicationEvents.stream(ProductQuantityChangedEvent.class)
                .findFirst()
                .orElseThrow();
        
        assertThat(event.productId()).isEqualTo(1L);
        assertThat(event.oldQuantity()).isEqualTo(100);
        assertThat(event.newQuantity()).isEqualTo(50);
        assertThat(event.productName()).isEqualTo("Laptop");
        assertThat(event.locationName()).isEqualTo("A1-01");
        assertThat(event.warehouseName()).isEqualTo("Main Warehouse");
    }

    /**
     * Tests updating product with duplicate name and expects IllegalArgumentException.
     */
    @Test
    void testUpdateProduct_DuplicateName_ThrowsException() {
        PatchProductRequest request = new PatchProductRequest(
                Optional.of("Existing Product"),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );

        when(productRepository.findByProductIdAndLocationId(1L, 1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.existsByNameAndLocation("Existing Product", testLocation)).thenReturn(true);

        assertThatThrownBy(() -> productService.updateProduct(1L, 1L, 1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    /**
     * Tests product search with query criteria and expects matching products.
     */
    @Test
    void testSearchProducts_Success() {
        ProductSearchCriteria criteria = ProductSearchCriteria.builder()
                .query("laptop")
                .organisationId(1L)
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> productPage = new PageImpl<>(Arrays.asList(testProduct));

        when(productRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(productPage);

        Page<Product> result = productService.searchProducts(criteria, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Laptop");
    }

    /**
     * Tests product search with multiple criteria and expects filtered results.
     */
    @Test
    void testSearchProducts_WithMultipleCriteria() {
        ProductSearchCriteria criteria = ProductSearchCriteria.builder()
                .query("laptop")
                .warehouseId(1L)
                .minPrice(1000.0)
                .maxPrice(2000.0)
                .isAvailable(true)
                .build();

        Pageable pageable = PageRequest.of(0, 20);
        Page<Product> productPage = new PageImpl<>(Arrays.asList(testProduct));

        when(productRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(productPage);

        Page<Product> result = productService.searchProducts(criteria, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(productRepository).findAll(any(Specification.class), eq(pageable));
    }
}
