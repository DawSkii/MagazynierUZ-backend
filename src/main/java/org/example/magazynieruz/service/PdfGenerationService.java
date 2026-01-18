package org.example.magazynieruz.service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.extern.slf4j.Slf4j;
import org.example.magazynieruz.dto.export.InventoryExportData;
import org.example.magazynieruz.dto.export.ProductInventoryData;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class PdfGenerationService {
    
    private static final DeviceRgb HEADER_COLOR = new DeviceRgb(41, 128, 185);
    private static final DeviceRgb LIGHT_GRAY = new DeviceRgb(240, 240, 240);
    private static final DeviceRgb WHITE = new DeviceRgb(255, 255, 255);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    
    public byte[] generateInventoryPdf(InventoryExportData data) {
        log.info("Generating PDF for {} products with scope: {}", data.products().size(), data.scope());
        
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            pdfDoc.setDefaultPageSize(PageSize.A4);
            
            pdfDoc.addEventHandler(PdfDocumentEvent.END_PAGE, new PageFooterEventHandler());
            
            Document document = new Document(pdfDoc);
            
            addHeader(document, data);
            
            addInventoryTable(document, data);
            
            addSummary(document, data);
            
            document.close();
            
            log.info("PDF generated successfully, size: {} bytes", baos.size());
            return baos.toByteArray();
            
        } catch (Exception e) {
            log.error("Error generating PDF", e);
            throw new RuntimeException("Failed to generate PDF: " + e.getMessage(), e);
        }
    }
    
    private void addHeader(Document document, InventoryExportData data) {
        Paragraph title = new Paragraph("Inventory Export Report")
                .setFontSize(20)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(5);
        document.add(title);
        
        Paragraph subtitle = new Paragraph("Organisation: " + data.organisationName())
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(3);
        document.add(subtitle);
        
        Paragraph exportInfo = new Paragraph()
                .add("Export Date: " + data.exportDate().format(DATE_FORMATTER) + "\n")
                .add("Scope: " + data.scope().name())
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(3);
        
        if (data.warehouseName() != null) {
            exportInfo.add("\nWarehouse: " + data.warehouseName());
        }
        if (data.locationName() != null) {
            exportInfo.add("\nLocation: " + data.locationName());
        }
        
        document.add(exportInfo);
        document.add(new Paragraph("\n").setMarginBottom(10));
    }
    
    private void addInventoryTable(Document document, InventoryExportData data) {
        float[] columnWidths = {1, 3, 2, 2, 3};
        Table table = new Table(UnitValue.createPercentArray(columnWidths))
                .useAllAvailableWidth()
                .setMarginBottom(20);
        
        addTableHeader(table, "ID", "Product Name", "Quantity", "Location", "Warehouse");
        
        boolean alternate = false;
        for (ProductInventoryData product : data.products()) {
            DeviceRgb rowColor = alternate ? LIGHT_GRAY : WHITE;
            
            addTableCell(table, String.valueOf(product.productId()), rowColor, false);
            addTableCell(table, product.productName(), rowColor, false);
            addTableCell(table, String.valueOf(product.quantity()), rowColor, true);
            addTableCell(table, product.locationName(), rowColor, false);
            addTableCell(table, product.warehouseName(), rowColor, false);
            
            alternate = !alternate;
        }
        
        document.add(table);
    }
    
    private void addTableHeader(Table table, String... headers) {
        for (String header : headers) {
            Cell cell = new Cell()
                    .add(new Paragraph(header).setBold().setFontColor(ColorConstants.WHITE))
                    .setBackgroundColor(HEADER_COLOR)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPadding(8);
            table.addHeaderCell(cell);
        }
    }
    
    private void addTableCell(Table table, String content, DeviceRgb backgroundColor, boolean center) {
        Cell cell = new Cell()
                .add(new Paragraph(content != null ? content : "N/A"))
                .setBackgroundColor(backgroundColor)
                .setPadding(6);
        
        if (center) {
            cell.setTextAlignment(TextAlignment.CENTER);
        }
        
        table.addCell(cell);
    }
    
    private void addSummary(Document document, InventoryExportData data) {
        int totalProducts = data.products().size();
        int totalQuantity = data.products().stream()
                .mapToInt(ProductInventoryData::quantity)
                .sum();
        
        Paragraph summary = new Paragraph()
                .add("Total Products: " + totalProducts + "\n")
                .add("Total Quantity: " + totalQuantity)
                .setFontSize(10)
                .setBold()
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginTop(10);
        
        document.add(summary);
    }
    
    private static class PageFooterEventHandler implements IEventHandler {
        @Override
        public void handleEvent(Event event) {
            PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
            PdfDocument pdfDoc = docEvent.getDocument();
            PdfPage page = docEvent.getPage();
            int pageNumber = pdfDoc.getPageNumber(page);
            
            PdfCanvas canvas = new PdfCanvas(page.newContentStreamBefore(), page.getResources(), pdfDoc);
            
            try {
                canvas.beginText()
                        .setFontAndSize(com.itextpdf.kernel.font.PdfFontFactory.createFont(
                                com.itextpdf.io.font.constants.StandardFonts.HELVETICA), 9)
                        .moveText(PageSize.A4.getWidth() / 2 - 20, 30)
                        .showText("Page " + pageNumber)
                        .endText();
            } catch (Exception e) {
                log.error("Error adding page footer", e);
            }
            
            canvas.release();
        }
    }
}
