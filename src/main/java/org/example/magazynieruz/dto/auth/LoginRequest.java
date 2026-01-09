package org.example.magazynieruz.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

@Schema(description = "Request to authenticate a user")
public record LoginRequest(
        @Schema(description = "Username for authentication", example = "john.doe")
        String username,

        @Schema(description = "Password for authentication", example = "SecurePass123!")
        String password
) implements Serializable {
}
