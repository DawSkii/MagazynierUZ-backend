package org.example.magazynieruz.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.magazynieruz.config.AlertConfig;
import org.springframework.stereotype.Service;

/**
 * Service for detecting low stock conditions.
 * Checks product quantities against configured thresholds.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LowStockDetectionService {

    private final AlertConfig alertConfig;

    /**
     * Determines if a quantity is below the low stock threshold.
     *
     * @param quantity the quantity to check
     * @return true if quantity is below threshold
     */
    public boolean isLowStock(int quantity) {
        int threshold = alertConfig.getLowStock().getThreshold();
        boolean isLow = quantity < threshold;
        log.debug("Checking if quantity {} is low stock (threshold: {}): {}", quantity, threshold, isLow);
        return isLow;
    }
}
