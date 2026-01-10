package org.example.magazynieruz.dto.address;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request containing structured address details")
public record CreateAddressRequest(
        @Schema(description = "Street name", example = "Main Street")
        String street,

        @Schema(description = "House number", example = "123")
        String houseNumber,

        @Schema(description = "Apartment number (optional)", example = "4B")
        String apartmentNumber,

        @Schema(description = "City name", example = "Warsaw")
        String city,

        @Schema(description = "Postal code", example = "00-001")
        String postcode,

        @Schema(description = "Geographical latitude coordinate", example = "52.2297")
        Double latitude,

        @Schema(description = "Geographical longitude coordinate", example = "21.0122")
        Double longitude
) {}