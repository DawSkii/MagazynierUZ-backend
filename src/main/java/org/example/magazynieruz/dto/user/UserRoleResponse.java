package org.example.magazynieruz.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

@Schema(description = "Response containing user role information")
public record UserRoleResponse(
        @Schema(description = "User ID", example = "1")
        Long userId,

        @Schema(description = "Username", example = "john_doe")
        String username,

        @Schema(description = "Set of role names assigned to the user", example = "[\"ROLE_USER\", \"ROLE_ADMIN\"]")
        Set<String> roles
) {}
