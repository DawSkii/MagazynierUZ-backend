package org.example.magazynieruz.controller;

import org.example.magazynieruz.dto.location.LocationResponse;
import org.example.magazynieruz.dto.product.ProductResponse;
import org.example.magazynieruz.dto.product.ProductSearchCriteria;
import org.example.magazynieruz.dto.warehouse.WarehouseResponse;
import org.example.magazynieruz.mapper.ProductMapper;
import org.example.magazynieruz.model.Location;
import org.example.magazynieruz.model.LocationType;
import org.example.magazynieruz.model.Product;
import org.example.magazynieruz.model.Warehouse;
import org.example.magazynieruz.security.UserContext;
import org.example.magazynieruz.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link ProductSearchController}.
 * Verifies product search functionality with various criteria and top products retrieval.
 */
@ExtendWith(MockitoExtension.class)
class ProductSearchControllerTest {

    @Mock
    private ProductService productService;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private UserContext userContext;

    @InjectMocks
    private ProductSearchController productSearchController;

    private Product product1;
    private Product product2;
    private ProductResponse productResponse1;
    private ProductResponse productResponse2;

    @BeforeEach
    void setUp() {
        Location location = new Location();
        location.setId(1L);
        location.setLocationCode("A1-01");

        Warehouse warehouse = new Warehouse();
        warehouse.setId(1L);
        warehouse.setWarehouseName("Main Warehouse");
        location.setWarehouse(warehouse);

        product1 = Product.builder()
                .productId(1L)
                .name("Laptop")
                .quantity(100)
                .price(1500.0)
                .description("Gaming laptop")
                .location(location)
                .build();

        product2 = Product.builder()
                .productId(2L)
                .name("Mouse")
                .quantity(50)
                .price(25.0)
                .description("Wireless mouse")
                .location(location)
                .build();

        LocationResponse locationResponse = new LocationResponse(1L, "A1-01", "Zone A", LocationType.PICKING, true, false);
        WarehouseResponse warehouseResponse = new WarehouseResponse(1L, "Main Warehouse", "WH-001", true, null);

        productResponse1 = new ProductResponse(1L, "Laptop", "Gaming laptop", 1500.0, 100, locationResponse, warehouseResponse);
        productResponse2 = new ProductResponse(2L, "Mouse", "Wireless mouse", 25.0, 50, locationResponse, warehouseResponse);
    }

    /**
     * Tests product search with query and warehouse ID filter and expects filtered results.
     */
    @Test
    void testSearchProducts_WithQueryAndWarehouseId() {
        Long organisationId = 1L;
        String query = "laptop";
        Long warehouseId = 1L;

        when(userContext.getCurrentOrganisationId()).thenReturn(organisationId);
        Page<Product> productPage = new PageImpl<>(Arrays.asList(product1));
        when(productService.searchProducts(any(ProductSearchCriteria.class), any(Pageable.class)))
                .thenReturn(productPage);
        when(productMapper.toResponse(product1)).thenReturn(productResponse1);

        ResponseEntity<Page<ProductResponse>> response = productSearchController.searchProducts(
                query, warehouseId, null, null, null, null, null, null,
                0, 20, "name", "asc"
        );

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
        assertThat(response.getBody().getContent().get(0).name()).isEqualTo("Laptop");

        ArgumentCaptor<ProductSearchCriteria> criteriaCaptor = ArgumentCaptor.forClass(ProductSearchCriteria.class);
        verify(productService).searchProducts(criteriaCaptor.capture(), any(Pageable.class));
        
        ProductSearchCriteria capturedCriteria = criteriaCaptor.getValue();
        assertThat(capturedCriteria.getQuery()).isEqualTo(query);
        assertThat(capturedCriteria.getWarehouseId()).isEqualTo(warehouseId);
        assertThat(capturedCriteria.getOrganisationId()).isEqualTo(organisationId);
    }

    /**
     * Tests product search with price range filter and expects products within specified range.
     */
    @Test
    void testSearchProducts_WithPriceRange() {
        Long organisationId = 1L;
        Double minPrice = 1000.0;
        Double maxPrice = 2000.0;

        when(userContext.getCurrentOrganisationId()).thenReturn(organisationId);
        Page<Product> productPage = new PageImpl<>(Arrays.asList(product1));
        when(productService.searchProducts(any(ProductSearchCriteria.class), any(Pageable.class)))
                .thenReturn(productPage);
        when(productMapper.toResponse(product1)).thenReturn(productResponse1);

        ResponseEntity<Page<ProductResponse>> response = productSearchController.searchProducts(
                null, null, null, minPrice, maxPrice, null, null, null,
                0, 20, "price", "asc"
        );

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        ArgumentCaptor<ProductSearchCriteria> criteriaCaptor = ArgumentCaptor.forClass(ProductSearchCriteria.class);
        verify(productService).searchProducts(criteriaCaptor.capture(), any(Pageable.class));
        
        ProductSearchCriteria capturedCriteria = criteriaCaptor.getValue();
        assertThat(capturedCriteria.getMinPrice()).isEqualTo(minPrice);
        assertThat(capturedCriteria.getMaxPrice()).isEqualTo(maxPrice);
    }

    /**
     * Tests retrieving top 10 products with default sorting and expects correct pagination.
     */
    @Test
    void testGetTop10Products_DefaultSorting() {
        Long organisationId = 1L;
        when(userContext.getCurrentOrganisationId()).thenReturn(organisationId);
        
        Page<Product> productPage = new PageImpl<>(Arrays.asList(product1, product2));
        when(productService.searchProducts(any(ProductSearchCriteria.class), any(Pageable.class)))
                .thenReturn(productPage);
        when(productMapper.toResponse(product1)).thenReturn(productResponse1);
        when(productMapper.toResponse(product2)).thenReturn(productResponse2);

        ResponseEntity<List<ProductResponse>> response = productSearchController.getTop10Products(
                "quantity", "desc", null, null, true
        );

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody().get(0).name()).isEqualTo("Laptop");
        assertThat(response.getBody().get(1).name()).isEqualTo("Mouse");

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(productService).searchProducts(any(ProductSearchCriteria.class), pageableCaptor.capture());
        
        Pageable capturedPageable = pageableCaptor.getValue();
        assertThat(capturedPageable.getPageSize()).isEqualTo(10);
        assertThat(capturedPageable.getPageNumber()).isEqualTo(0);
    }

    /**
     * Tests retrieving top 10 products filtered by warehouse and expects warehouse-specific results.
     */
    @Test
    void testGetTop10Products_WithWarehouseFilter() {
        Long organisationId = 1L;
        Long warehouseId = 1L;
        when(userContext.getCurrentOrganisationId()).thenReturn(organisationId);
        
        Page<Product> productPage = new PageImpl<>(Arrays.asList(product1));
        when(productService.searchProducts(any(ProductSearchCriteria.class), any(Pageable.class)))
                .thenReturn(productPage);
        when(productMapper.toResponse(product1)).thenReturn(productResponse1);

        ResponseEntity<List<ProductResponse>> response = productSearchController.getTop10Products(
                "price", "desc", warehouseId, null, true
        );

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).hasSize(1);

        ArgumentCaptor<ProductSearchCriteria> criteriaCaptor = ArgumentCaptor.forClass(ProductSearchCriteria.class);
        verify(productService).searchProducts(criteriaCaptor.capture(), any(Pageable.class));
        
        ProductSearchCriteria capturedCriteria = criteriaCaptor.getValue();
        assertThat(capturedCriteria.getWarehouseId()).isEqualTo(warehouseId);
        assertThat(capturedCriteria.getIsAvailable()).isTrue();
    }

    /**
     * Tests that providing an invalid sort field defaults to quantity sorting.
     */
    @Test
    void testGetTop10Products_InvalidSortField_UsesDefault() {
        Long organisationId = 1L;
        when(userContext.getCurrentOrganisationId()).thenReturn(organisationId);
        
        Page<Product> productPage = new PageImpl<>(Arrays.asList(product1));
        when(productService.searchProducts(any(ProductSearchCriteria.class), any(Pageable.class)))
                .thenReturn(productPage);
        when(productMapper.toResponse(product1)).thenReturn(productResponse1);

        ResponseEntity<List<ProductResponse>> response = productSearchController.getTop10Products(
                "invalid_field", "desc", null, null, true
        );

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(productService).searchProducts(any(ProductSearchCriteria.class), pageableCaptor.capture());
        
        Pageable capturedPageable = pageableCaptor.getValue();
        assertThat(capturedPageable.getSort().toString()).contains("quantity");
    }
}
