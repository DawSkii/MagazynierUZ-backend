package org.example.magazynieruz.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

import java.util.Set;

@Schema(description = "Request to update user by admin")
public record AdminUpdateUserRequest(
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        @Schema(description = "New username (optional)", example = "updated_user")
        String username,

        @Size(min = 4, message = "Password must be at least 4 characters")
        @Schema(description = "New password (optional)", example = "newpassword123")
        String password,

        @Schema(description = "Organisation ID to assign the user to (optional, null to remove)", example = "2")
        Long organisationId,

        @Schema(description = "Set of role names to assign (optional)", example = "[\"ROLE_USER\", \"ROLE_ADMIN\"]")
        Set<String> roleNames
) {}
