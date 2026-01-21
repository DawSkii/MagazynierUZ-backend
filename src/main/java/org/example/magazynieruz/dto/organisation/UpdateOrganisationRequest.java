package org.example.magazynieruz.dto.organisation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to update an existing organisation")
public record UpdateOrganisationRequest(
        @Size(max = 20, message = "Organisation name must not exceed 20 characters")
        @Schema(description = "Name of the organisation", example = "TechCorp Updated")
        String name,

        @Size(max = 20, message = "TIN must not exceed 20 characters")
        @Schema(description = "Tax Identification Number", example = "9876543210")
        String tin
) {}
