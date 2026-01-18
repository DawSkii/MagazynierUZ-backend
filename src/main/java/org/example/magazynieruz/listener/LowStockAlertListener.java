package org.example.magazynieruz.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.magazynieruz.config.AlertConfig;
import org.example.magazynieruz.event.ProductQuantityChangedEvent;
import org.example.magazynieruz.service.EmailAlertService;
import org.example.magazynieruz.service.LowStockDetectionService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LowStockAlertListener {

    private final AlertConfig alertConfig;
    private final EmailAlertService emailAlertService;
    private final LowStockDetectionService lowStockDetectionService;

    @EventListener
    public void handleProductQuantityChanged(ProductQuantityChangedEvent event) {
        log.debug("Received ProductQuantityChangedEvent for product: {} (ID: {}), quantity changed from {} to {}",
                event.productName(), event.productId(), event.oldQuantity(), event.newQuantity());

        if (!alertConfig.getLowStock().isEventDriven()) {
            log.debug("Event-driven alerts are disabled. Skipping alert for product: {}", event.productName());
            return;
        }

        int oldQty = event.oldQuantity() != null ? event.oldQuantity() : 0;
        int newQty = event.newQuantity() != null ? event.newQuantity() : 0;

        boolean isNewQuantityLow = lowStockDetectionService.isLowStock(newQty);
        // If oldQuantity is null, treat it as "not low" to trigger alerts for new products with low stock
        boolean wasOldQuantityLow = event.oldQuantity() != null && lowStockDetectionService.isLowStock(oldQty);

        log.debug("Product: {}, Old Qty: {} (Low: {}), New Qty: {} (Low: {})",
                event.productName(), oldQty, wasOldQuantityLow, newQty, isNewQuantityLow);

        if (isNewQuantityLow && (!wasOldQuantityLow || newQty < oldQty)) {
            log.info("Triggering low stock alert for product: {} (Quantity: {} -> {})",
                    event.productName(), oldQty, newQty);

            int threshold = alertConfig.getLowStock().getThreshold();
            emailAlertService.sendLowStockAlert(
                    event.productName(),
                    newQty,
                    threshold,
                    event.locationName(),
                    event.warehouseName()
            );

            log.info("Low stock alert triggered for product: {} at location: {}, warehouse: {}",
                    event.productName(), event.locationName(), event.warehouseName());
        } else {
            log.debug("No alert needed for product: {} - conditions not met", event.productName());
        }
    }
}
