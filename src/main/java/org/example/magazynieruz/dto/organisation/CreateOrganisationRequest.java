package org.example.magazynieruz.dto.organisation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to create a new organisation")
public record CreateOrganisationRequest(
        @NotBlank(message = "Organisation name is required")
        @Size(max = 20, message = "Organisation name must not exceed 20 characters")
        @Schema(description = "Name of the organisation", example = "TechCorp")
        String name,

        @NotBlank(message = "TIN is required")
        @Size(max = 20, message = "TIN must not exceed 20 characters")
        @Schema(description = "Tax Identification Number", example = "1234567890")
        String tin
) {}
