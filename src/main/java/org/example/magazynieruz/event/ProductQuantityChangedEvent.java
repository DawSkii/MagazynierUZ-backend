package org.example.magazynieruz.event;

public record ProductQuantityChangedEvent(Long productId, String productName, Integer oldQuantity, Integer newQuantity,
                                          String locationName, String warehouseName, Long organisationId) {

}
