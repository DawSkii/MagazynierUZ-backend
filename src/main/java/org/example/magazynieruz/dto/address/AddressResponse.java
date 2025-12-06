package org.example.magazynieruz.dto.address;

public record AddressResponse(
        String street,
        String houseNumber,
        String apartmentNumber,
        String city,
        String postcode,
        Double latitude,
        Double longitude
) {}