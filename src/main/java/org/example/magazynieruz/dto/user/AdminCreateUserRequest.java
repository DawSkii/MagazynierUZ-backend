package org.example.magazynieruz.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

@Schema(description = "Request to create a new user by admin")
public record AdminCreateUserRequest(
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        @Schema(description = "Username for the new user", example = "new_user")
        String username,

        @NotBlank(message = "Password is required")
        @Size(min = 4, message = "Password must be at least 4 characters")
        @Schema(description = "Password for the new user", example = "password123")
        String password,

        @Schema(description = "Organisation ID to assign the user to (optional)", example = "1")
        Long organisationId,

        @Schema(description = "Set of role names to assign (e.g., ROLE_USER, ROLE_ADMIN)", example = "[\"ROLE_USER\"]")
        Set<String> roleNames
) {}
