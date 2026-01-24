package org.example.magazynieruz.controller;

import org.example.magazynieruz.dto.export.ExportScope;
import org.example.magazynieruz.dto.export.InventoryExportData;
import org.example.magazynieruz.dto.export.InventoryExportRequest;
import org.example.magazynieruz.dto.export.ProductInventoryData;
import org.example.magazynieruz.security.UserContext;
import org.example.magazynieruz.service.InventoryExportService;
import org.example.magazynieruz.service.PdfGenerationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link InventoryExportController}.
 * Verifies inventory export functionality for different scopes and PDF generation.
 */
@ExtendWith(MockitoExtension.class)
class InventoryExportControllerTest {

    @Mock
    private InventoryExportService inventoryExportService;

    @Mock
    private PdfGenerationService pdfGenerationService;

    @Mock
    private UserContext userContext;

    @InjectMocks
    private InventoryExportController inventoryExportController;

    private InventoryExportData testInventoryData;
    private byte[] testPdfBytes;

    @BeforeEach
    void setUp() {
        List<ProductInventoryData> products = Arrays.asList(
                new ProductInventoryData(1L, "Laptop", 100, "A1-01", "Main Warehouse"),
                new ProductInventoryData(2L, "Mouse", 200, "A1-02", "Main Warehouse")
        );

        testInventoryData = new InventoryExportData(
                ExportScope.WAREHOUSE,
                LocalDateTime.now(),
                "Test Organisation",
                "Main Warehouse",
                null,
                products
        );

        testPdfBytes = "PDF_CONTENT".getBytes();
    }

    /**
     * Tests inventory export to PDF with warehouse scope and expects correct PDF with appropriate headers.
     */
    @Test
    void testExportInventoryToPdf_WarehouseScope() {
        Long organisationId = 1L;
        Long warehouseId = 1L;
        ExportScope scope = ExportScope.WAREHOUSE;

        when(userContext.getCurrentOrganisationId()).thenReturn(organisationId);
        when(inventoryExportService.getInventoryData(any(InventoryExportRequest.class), eq(organisationId)))
                .thenReturn(testInventoryData);
        when(pdfGenerationService.generateInventoryPdf(testInventoryData)).thenReturn(testPdfBytes);

        ResponseEntity<byte[]> response = inventoryExportController.exportInventoryToPdf(
                scope, warehouseId, null
        );

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isEqualTo(testPdfBytes);
        assertThat(response.getHeaders().getContentType().toString()).contains("application/pdf");
        
        String contentDisposition = response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);
        assertThat(contentDisposition).contains("attachment");
        assertThat(contentDisposition).contains("inventory_warehouse_" + warehouseId);
        assertThat(contentDisposition).contains(".pdf");

        ArgumentCaptor<InventoryExportRequest> requestCaptor = ArgumentCaptor.forClass(InventoryExportRequest.class);
        verify(inventoryExportService).getInventoryData(requestCaptor.capture(), eq(organisationId));
        
        InventoryExportRequest capturedRequest = requestCaptor.getValue();
        assertThat(capturedRequest.scope()).isEqualTo(scope);
        assertThat(capturedRequest.warehouseId()).isEqualTo(warehouseId);
        assertThat(capturedRequest.locationId()).isNull();
    }

    /**
     * Tests inventory export to PDF with location scope and expects correct filename in response headers.
     */
    @Test
    void testExportInventoryToPdf_LocationScope() {
        Long organisationId = 1L;
        Long warehouseId = 1L;
        Long locationId = 5L;
        ExportScope scope = ExportScope.LOCATION;

        InventoryExportData locationData = new InventoryExportData(
                ExportScope.LOCATION,
                LocalDateTime.now(),
                "Test Organisation",
                "Main Warehouse",
                "A1-01",
                testInventoryData.products()
        );

        when(userContext.getCurrentOrganisationId()).thenReturn(organisationId);
        when(inventoryExportService.getInventoryData(any(InventoryExportRequest.class), eq(organisationId)))
                .thenReturn(locationData);
        when(pdfGenerationService.generateInventoryPdf(locationData)).thenReturn(testPdfBytes);

        ResponseEntity<byte[]> response = inventoryExportController.exportInventoryToPdf(
                scope, warehouseId, locationId
        );

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isEqualTo(testPdfBytes);
        
        String contentDisposition = response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);
        assertThat(contentDisposition).contains("inventory_location_" + locationId);

        ArgumentCaptor<InventoryExportRequest> requestCaptor = ArgumentCaptor.forClass(InventoryExportRequest.class);
        verify(inventoryExportService).getInventoryData(requestCaptor.capture(), eq(organisationId));
        
        InventoryExportRequest capturedRequest = requestCaptor.getValue();
        assertThat(capturedRequest.scope()).isEqualTo(scope);
        assertThat(capturedRequest.warehouseId()).isEqualTo(warehouseId);
        assertThat(capturedRequest.locationId()).isEqualTo(locationId);
    }

    /**
     * Tests inventory export to PDF with organisation scope and expects organisation-wide inventory data.
     */
    @Test
    void testExportInventoryToPdf_OrganisationScope() {
        Long organisationId = 1L;
        ExportScope scope = ExportScope.ORGANISATION;

        InventoryExportData organisationData = new InventoryExportData(
                ExportScope.ORGANISATION,
                LocalDateTime.now(),
                "Test Organisation",
                null,
                null,
                testInventoryData.products()
        );

        when(userContext.getCurrentOrganisationId()).thenReturn(organisationId);
        when(inventoryExportService.getInventoryData(any(InventoryExportRequest.class), eq(organisationId)))
                .thenReturn(organisationData);
        when(pdfGenerationService.generateInventoryPdf(organisationData)).thenReturn(testPdfBytes);

        ResponseEntity<byte[]> response = inventoryExportController.exportInventoryToPdf(
                scope, null, null
        );

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isEqualTo(testPdfBytes);
        
        String contentDisposition = response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);
        assertThat(contentDisposition).contains("inventory_organisation_");

        ArgumentCaptor<InventoryExportRequest> requestCaptor = ArgumentCaptor.forClass(InventoryExportRequest.class);
        verify(inventoryExportService).getInventoryData(requestCaptor.capture(), eq(organisationId));
        
        InventoryExportRequest capturedRequest = requestCaptor.getValue();
        assertThat(capturedRequest.scope()).isEqualTo(scope);
        assertThat(capturedRequest.warehouseId()).isNull();
        assertThat(capturedRequest.locationId()).isNull();
    }

    /**
     * Tests that PDF generation service is invoked and content length header is correctly set.
     */
    @Test
    void testExportInventoryToPdf_VerifyPdfGeneration() {
        Long organisationId = 1L;
        Long warehouseId = 1L;
        ExportScope scope = ExportScope.WAREHOUSE;

        when(userContext.getCurrentOrganisationId()).thenReturn(organisationId);
        when(inventoryExportService.getInventoryData(any(InventoryExportRequest.class), eq(organisationId)))
                .thenReturn(testInventoryData);
        when(pdfGenerationService.generateInventoryPdf(testInventoryData)).thenReturn(testPdfBytes);

        ResponseEntity<byte[]> response = inventoryExportController.exportInventoryToPdf(
                scope, warehouseId, null
        );

        verify(pdfGenerationService).generateInventoryPdf(testInventoryData);
        assertThat(response.getHeaders().getContentLength()).isEqualTo(testPdfBytes.length);
    }
}
