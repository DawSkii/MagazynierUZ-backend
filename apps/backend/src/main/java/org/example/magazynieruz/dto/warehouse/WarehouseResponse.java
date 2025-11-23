package org.example.magazynieruz.dto.warehouse;

import org.example.magazynieruz.dto.address.AddressResponse;

public record WarehouseResponse(
        Long id,
        String name,
        String code,
        boolean isActive,
        AddressResponse address
) {}