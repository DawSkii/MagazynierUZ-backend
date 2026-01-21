package org.example.magazynieruz.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

@Schema(description = "Response containing user details")
public record UserResponse(
        @Schema(description = "User ID", example = "1")
        Long id,

        @Schema(description = "Username", example = "john_doe")
        String username,

        @Schema(description = "Organisation ID (null if not assigned)", example = "1")
        Long organisationId,

        @Schema(description = "Organisation name (null if not assigned)", example = "TechCorp")
        String organisationName,

        @Schema(description = "Set of role names", example = "[\"ROLE_USER\"]")
        Set<String> roles
) {}
