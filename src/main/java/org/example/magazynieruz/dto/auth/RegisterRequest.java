package org.example.magazynieruz.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

@Schema(description = "Request to register a new user")
public record RegisterRequest(
        @Schema(description = "Desired username for the new account", example = "john.doe")
        String username,

        @Schema(description = "Password for the new account", example = "SecurePass123!")
        String password
) implements Serializable {
}
