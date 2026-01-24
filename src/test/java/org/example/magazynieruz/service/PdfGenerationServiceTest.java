package org.example.magazynieruz.service;

import org.example.magazynieruz.dto.export.ExportScope;
import org.example.magazynieruz.dto.export.InventoryExportData;
import org.example.magazynieruz.dto.export.ProductInventoryData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link PdfGenerationService}.
 * Verifies PDF generation for inventory exports with various scopes and data.
 */
@ExtendWith(MockitoExtension.class)
class PdfGenerationServiceTest {

    private PdfGenerationService pdfGenerationService;

    @BeforeEach
    void setUp() {
        pdfGenerationService = new PdfGenerationService();
    }

    /**
     * Tests PDF generation with warehouse scope and multiple products and expects valid PDF output.
     */
    @Test
    void testGenerateInventoryPdf_Success() {
        List<ProductInventoryData> products = Arrays.asList(
                new ProductInventoryData(1L, "Laptop", 100, "A1-01", "Main Warehouse"),
                new ProductInventoryData(2L, "Mouse", 200, "A1-02", "Main Warehouse"),
                new ProductInventoryData(3L, "Keyboard", 150, "A1-03", "Main Warehouse")
        );

        InventoryExportData exportData = new InventoryExportData(
                ExportScope.WAREHOUSE,
                LocalDateTime.now(),
                "Test Organisation",
                "Main Warehouse",
                null,
                products
        );

        byte[] pdfBytes = pdfGenerationService.generateInventoryPdf(exportData);

        assertThat(pdfBytes).isNotNull();
        assertThat(pdfBytes.length).isGreaterThan(0);
        assertThat(new String(pdfBytes, 0, 4)).isEqualTo("%PDF");
    }

    /**
     * Tests PDF generation with location scope and expects valid PDF with location details.
     */
    @Test
    void testGenerateInventoryPdf_WithLocationScope() {
        List<ProductInventoryData> products = Arrays.asList(
                new ProductInventoryData(1L, "Product A", 50, "A1-01", "Warehouse 1")
        );

        InventoryExportData exportData = new InventoryExportData(
                ExportScope.LOCATION,
                LocalDateTime.now(),
                "Organisation ABC",
                "Warehouse 1",
                "A1-01",
                products
        );

        byte[] pdfBytes = pdfGenerationService.generateInventoryPdf(exportData);

        assertThat(pdfBytes).isNotNull();
        assertThat(pdfBytes.length).isGreaterThan(0);
        assertThat(new String(pdfBytes, 0, 4)).isEqualTo("%PDF");
    }

    /**
     * Tests PDF generation with organisation scope and expects valid organisation-wide report.
     */
    @Test
    void testGenerateInventoryPdf_WithOrganisationScope() {
        List<ProductInventoryData> products = Arrays.asList(
                new ProductInventoryData(1L, "Item 1", 10, "L1", "W1"),
                new ProductInventoryData(2L, "Item 2", 20, "L2", "W2")
        );

        InventoryExportData exportData = new InventoryExportData(
                ExportScope.ORGANISATION,
                LocalDateTime.now(),
                "Global Organisation",
                null,
                null,
                products
        );

        byte[] pdfBytes = pdfGenerationService.generateInventoryPdf(exportData);

        assertThat(pdfBytes).isNotNull();
        assertThat(pdfBytes.length).isGreaterThan(0);
    }

    /**
     * Tests PDF generation with empty product list and expects valid PDF structure.
     */
    @Test
    void testGenerateInventoryPdf_EmptyProducts() {
        InventoryExportData exportData = new InventoryExportData(
                ExportScope.WAREHOUSE,
                LocalDateTime.now(),
                "Test Organisation",
                "Main Warehouse",
                null,
                List.of()
        );

        byte[] pdfBytes = pdfGenerationService.generateInventoryPdf(exportData);

        assertThat(pdfBytes).isNotNull();
        assertThat(pdfBytes.length).isGreaterThan(0);
    }

    /**
     * Tests PDF generation with large product dataset and expects PDF of substantial size.
     */
    @Test
    void testGenerateInventoryPdf_LargeDataset() {
        List<ProductInventoryData> products = Arrays.asList(
                new ProductInventoryData(1L, "Product 1", 100, "A1-01", "Warehouse"),
                new ProductInventoryData(2L, "Product 2", 200, "A1-02", "Warehouse"),
                new ProductInventoryData(3L, "Product 3", 300, "A1-03", "Warehouse"),
                new ProductInventoryData(4L, "Product 4", 400, "A1-04", "Warehouse"),
                new ProductInventoryData(5L, "Product 5", 500, "A1-05", "Warehouse"),
                new ProductInventoryData(6L, "Product 6", 600, "A1-06", "Warehouse"),
                new ProductInventoryData(7L, "Product 7", 700, "A1-07", "Warehouse"),
                new ProductInventoryData(8L, "Product 8", 800, "A1-08", "Warehouse"),
                new ProductInventoryData(9L, "Product 9", 900, "A1-09", "Warehouse"),
                new ProductInventoryData(10L, "Product 10", 1000, "A1-10", "Warehouse")
        );

        InventoryExportData exportData = new InventoryExportData(
                ExportScope.WAREHOUSE,
                LocalDateTime.now(),
                "Test Organisation",
                "Main Warehouse",
                null,
                products
        );

        byte[] pdfBytes = pdfGenerationService.generateInventoryPdf(exportData);

        assertThat(pdfBytes).isNotNull();
        assertThat(pdfBytes.length).isGreaterThan(1000);
    }

    /**
     * Tests PDF generation with null product name and expects graceful handling.
     */
    @Test
    void testGenerateInventoryPdf_WithNullProductName() {
        List<ProductInventoryData> products = Arrays.asList(
                new ProductInventoryData(1L, null, 100, "A1-01", "Warehouse")
        );

        InventoryExportData exportData = new InventoryExportData(
                ExportScope.WAREHOUSE,
                LocalDateTime.now(),
                "Test Organisation",
                "Main Warehouse",
                null,
                products
        );

        byte[] pdfBytes = pdfGenerationService.generateInventoryPdf(exportData);

        assertThat(pdfBytes).isNotNull();
        assertThat(pdfBytes.length).isGreaterThan(0);
    }
}
