package org.example.magazynieruz.service;

import org.example.magazynieruz.config.AlertConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link LowStockDetectionService}.
 * Verifies low stock detection logic based on configurable threshold.
 */
@ExtendWith(MockitoExtension.class)
class LowStockDetectionServiceTest {

    @Mock
    private AlertConfig alertConfig;

    @Mock
    private AlertConfig.LowStock lowStockConfig;

    @InjectMocks
    private LowStockDetectionService lowStockDetectionService;

    @BeforeEach
    void setUp() {
        when(alertConfig.getLowStock()).thenReturn(lowStockConfig);
    }

    /**
     * Tests low stock detection with quantity below threshold and expects true.
     */
    @Test
    void testIsLowStock_BelowThreshold_ReturnsTrue() {
        int threshold = 20;
        int quantity = 15;
        when(lowStockConfig.getThreshold()).thenReturn(threshold);

        boolean result = lowStockDetectionService.isLowStock(quantity);

        assertThat(result).isTrue();
    }

    /**
     * Tests low stock detection with quantity at threshold and expects false.
     */
    @Test
    void testIsLowStock_AtThreshold_ReturnsFalse() {
        int threshold = 20;
        int quantity = 20;
        when(lowStockConfig.getThreshold()).thenReturn(threshold);

        boolean result = lowStockDetectionService.isLowStock(quantity);

        assertThat(result).isFalse();
    }

    /**
     * Tests low stock detection with quantity above threshold and expects false.
     */
    @Test
    void testIsLowStock_AboveThreshold_ReturnsFalse() {
        int threshold = 20;
        int quantity = 25;
        when(lowStockConfig.getThreshold()).thenReturn(threshold);

        boolean result = lowStockDetectionService.isLowStock(quantity);

        assertThat(result).isFalse();
    }

    /**
     * Tests low stock detection with zero quantity and expects true.
     */
    @Test
    void testIsLowStock_ZeroQuantity_ReturnsTrue() {
        int threshold = 20;
        int quantity = 0;
        when(lowStockConfig.getThreshold()).thenReturn(threshold);

        boolean result = lowStockDetectionService.isLowStock(quantity);

        assertThat(result).isTrue();
    }

    /**
     * Tests low stock detection with quantity just below threshold and expects true.
     */
    @Test
    void testIsLowStock_JustBelowThreshold_ReturnsTrue() {
        int threshold = 20;
        int quantity = 19;
        when(lowStockConfig.getThreshold()).thenReturn(threshold);

        boolean result = lowStockDetectionService.isLowStock(quantity);

        assertThat(result).isTrue();
    }
}
