package org.example.magazynieruz.dto.organisation;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response containing organisation details")
public record OrganisationResponse(
        @Schema(description = "Unique identifier of the organisation", example = "1")
        Long id,

        @Schema(description = "Name of the organisation", example = "TechCorp")
        String name,

        @Schema(description = "Tax Identification Number", example = "1234567890")
        String tin
) {}
