package org.example.magazynieruz.dto.warehouse;

import org.example.magazynieruz.dto.address.CreateAddressRequest;

public record CreateWarehouseRequest(
        String name,
        String code,
        String description,
        CreateAddressRequest address
) {}
