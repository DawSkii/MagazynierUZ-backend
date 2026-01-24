package org.example.magazynieruz.listener;

import org.example.magazynieruz.config.AlertConfig;
import org.example.magazynieruz.event.ProductQuantityChangedEvent;
import org.example.magazynieruz.service.EmailAlertService;
import org.example.magazynieruz.service.LowStockDetectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

/**
 * Tests for {@link LowStockAlertListener}.
 * Verifies low stock alert triggering based on product quantity changes.
 */
@ExtendWith(MockitoExtension.class)
class LowStockAlertListenerTest {

    @Mock
    private AlertConfig alertConfig;

    @Mock
    private AlertConfig.LowStock lowStockConfig;

    @Mock
    private EmailAlertService emailAlertService;

    @Mock
    private LowStockDetectionService lowStockDetectionService;

    @InjectMocks
    private LowStockAlertListener lowStockAlertListener;

    @BeforeEach
    void setUp() {
        when(alertConfig.getLowStock()).thenReturn(lowStockConfig);
    }

    /**
     * Tests that low stock alert is triggered when quantity drops below threshold.
     */
    @Test
    void testHandleProductQuantityChanged_TriggerAlert_WhenQuantityBecomesLow() {
        ProductQuantityChangedEvent event = new ProductQuantityChangedEvent(
                1L, "Laptop", 50, 15, "A1-01", "Main Warehouse", 1L
        );

        when(lowStockConfig.isEventDriven()).thenReturn(true);
        when(lowStockConfig.getThreshold()).thenReturn(20);
        when(lowStockDetectionService.isLowStock(15)).thenReturn(true);
        when(lowStockDetectionService.isLowStock(50)).thenReturn(false);

        lowStockAlertListener.handleProductQuantityChanged(event);

        verify(emailAlertService).sendLowStockAlert("Laptop", 15, 20, "A1-01", "Main Warehouse");
    }

    /**
     * Tests that no alert is sent when event-driven alerts are disabled.
     */
    @Test
    void testHandleProductQuantityChanged_NoAlert_WhenEventDrivenDisabled() {
        ProductQuantityChangedEvent event = new ProductQuantityChangedEvent(
                1L, "Laptop", 50, 15, "A1-01", "Main Warehouse", 1L
        );

        when(lowStockConfig.isEventDriven()).thenReturn(false);

        lowStockAlertListener.handleProductQuantityChanged(event);

        verify(emailAlertService, never()).sendLowStockAlert(anyString(), anyInt(), anyInt(), anyString(), anyString());
        verify(lowStockDetectionService, never()).isLowStock(anyInt());
    }

    /**
     * Tests that no alert is sent when quantity remains above threshold.
     */
    @Test
    void testHandleProductQuantityChanged_NoAlert_WhenQuantityStillAboveThreshold() {
        ProductQuantityChangedEvent event = new ProductQuantityChangedEvent(
                1L, "Laptop", 100, 90, "A1-01", "Main Warehouse", 1L
        );

        when(lowStockConfig.isEventDriven()).thenReturn(true);
        when(lowStockDetectionService.isLowStock(90)).thenReturn(false);
        when(lowStockDetectionService.isLowStock(100)).thenReturn(false);

        lowStockAlertListener.handleProductQuantityChanged(event);

        verify(emailAlertService, never()).sendLowStockAlert(anyString(), anyInt(), anyInt(), anyString(), anyString());
    }

    /**
     * Tests that no alert is sent when quantity increases even if still below threshold.
     */
    @Test
    void testHandleProductQuantityChanged_NoAlert_WhenQuantityIncreases() {
        ProductQuantityChangedEvent event = new ProductQuantityChangedEvent(
                1L, "Laptop", 10, 15, "A1-01", "Main Warehouse", 1L
        );

        when(lowStockConfig.isEventDriven()).thenReturn(true);
        when(lowStockDetectionService.isLowStock(15)).thenReturn(true);
        when(lowStockDetectionService.isLowStock(10)).thenReturn(true);

        lowStockAlertListener.handleProductQuantityChanged(event);

        verify(emailAlertService, never()).sendLowStockAlert(anyString(), anyInt(), anyInt(), anyString(), anyString());
    }

    /**
     * Tests that alert is triggered when quantity decreases while already below threshold.
     */
    @Test
    void testHandleProductQuantityChanged_TriggerAlert_WhenBothQuantitiesLowButDecreased() {
        ProductQuantityChangedEvent event = new ProductQuantityChangedEvent(
                1L, "Laptop", 15, 10, "A1-01", "Main Warehouse", 1L
        );

        when(lowStockConfig.isEventDriven()).thenReturn(true);
        when(lowStockConfig.getThreshold()).thenReturn(20);
        when(lowStockDetectionService.isLowStock(10)).thenReturn(true);
        when(lowStockDetectionService.isLowStock(15)).thenReturn(true);

        lowStockAlertListener.handleProductQuantityChanged(event);

        verify(emailAlertService).sendLowStockAlert("Laptop", 10, 20, "A1-01", "Main Warehouse");
    }

    /**
     * Tests that alert is triggered for new product with low initial stock.
     */
    @Test
    void testHandleProductQuantityChanged_TriggerAlert_WhenOldQuantityNull() {
        ProductQuantityChangedEvent event = new ProductQuantityChangedEvent(
                1L, "Laptop", null, 15, "A1-01", "Main Warehouse", 1L
        );

        when(lowStockConfig.isEventDriven()).thenReturn(true);
        when(lowStockConfig.getThreshold()).thenReturn(20);
        when(lowStockDetectionService.isLowStock(15)).thenReturn(true);

        lowStockAlertListener.handleProductQuantityChanged(event);

        verify(emailAlertService).sendLowStockAlert("Laptop", 15, 20, "A1-01", "Main Warehouse");
    }

    /**
     * Tests that null new quantity is treated as zero for stock detection.
     */
    @Test
    void testHandleProductQuantityChanged_HandlesNullNewQuantity() {
        ProductQuantityChangedEvent event = new ProductQuantityChangedEvent(
                1L, "Laptop", 50, null, "A1-01", "Main Warehouse", 1L
        );

        when(lowStockConfig.isEventDriven()).thenReturn(true);
        when(lowStockDetectionService.isLowStock(0)).thenReturn(true);
        when(lowStockDetectionService.isLowStock(50)).thenReturn(false);

        lowStockAlertListener.handleProductQuantityChanged(event);

        verify(lowStockDetectionService).isLowStock(0);
    }
}
