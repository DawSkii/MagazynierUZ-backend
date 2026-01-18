package org.example.magazynieruz.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.magazynieruz.dto.export.ExportScope;
import org.example.magazynieruz.dto.export.InventoryExportData;
import org.example.magazynieruz.dto.export.InventoryExportRequest;
import org.example.magazynieruz.security.UserContext;
import org.example.magazynieruz.service.InventoryExportService;
import org.example.magazynieruz.service.PdfGenerationService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@RestController
@RequestMapping("/api/v1/exports")
@RequiredArgsConstructor
@Tag(name = "Exports", description = "Data export endpoints")
@SecurityRequirement(name = "bearer-jwt")
public class InventoryExportController {
    
    private final InventoryExportService inventoryExportService;
    private final PdfGenerationService pdfGenerationService;
    private final UserContext userContext;
    
    private static final DateTimeFormatter FILENAME_DATE_FORMAT = DateTimeFormatter.ofPattern("ddMMyyyy_HHmmss");
    
    @GetMapping("/inventory/pdf")
    @Operation(
        summary = "Export inventory to PDF", 
        description = "Exports warehouse inventory data to PDF format. Supports export at organisation, warehouse, or location level."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "PDF generated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request - missing required parameters"),
        @ApiResponse(responseCode = "403", description = "Access denied - unauthorized"),
        @ApiResponse(responseCode = "404", description = "Warehouse or location not found")
    })
    public ResponseEntity<byte[]> exportInventoryToPdf(
            @Parameter(description = "Export scope (ORGANISATION, WAREHOUSE, or LOCATION)", required = true, example = "WAREHOUSE")
            @RequestParam ExportScope scope,
            
            @Parameter(description = "Warehouse ID (required when scope is WAREHOUSE or LOCATION)", example = "1")
            @RequestParam(required = false) Long warehouseId,
            
            @Parameter(description = "Location ID (required when scope is LOCATION)", example = "5")
            @RequestParam(required = false) Long locationId
    ) {
        log.info("Received inventory export request - scope: {}, warehouseId: {}, locationId: {}", 
                scope, warehouseId, locationId);
        
        Long organisationId = userContext.getCurrentOrganisationId();
        
        InventoryExportRequest request = new InventoryExportRequest(scope, warehouseId, locationId);
        
        InventoryExportData inventoryData = inventoryExportService.getInventoryData(request, organisationId);
        
        byte[] pdfBytes = pdfGenerationService.generateInventoryPdf(inventoryData);
        
        String filename = generateFilename(scope, warehouseId, locationId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(pdfBytes.length);
        
        log.info("Successfully generated PDF: {} ({} bytes)", filename, pdfBytes.length);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
    
    private String generateFilename(ExportScope scope, Long warehouseId, Long locationId) {
        String timestamp = LocalDateTime.now().format(FILENAME_DATE_FORMAT);
        
        return switch (scope) {
            case ORGANISATION -> "inventory_organisation_" + timestamp + ".pdf";
            case WAREHOUSE -> "inventory_warehouse_" + warehouseId + "_" + timestamp + ".pdf";
            case LOCATION -> "inventory_location_" + locationId + "_" + timestamp + ".pdf";
        };
    }
}
