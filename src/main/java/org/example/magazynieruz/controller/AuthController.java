package org.example.magazynieruz.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.magazynieruz.dto.auth.LoginRequest;
import org.example.magazynieruz.dto.auth.LoginResponse;
import org.example.magazynieruz.dto.auth.RegisterRequest;
import org.example.magazynieruz.model.User;
import org.example.magazynieruz.service.JwtService;
import org.example.magazynieruz.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for user authentication and registration.
 * Handles user registration and JWT-based authentication.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User authentication and registration endpoints")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;

    /**
     * Registers a new user in the system.
     *
     * @param registerRequest the user registration details
     * @return ResponseEntity indicating successful registration
     */
    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new user account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid registration data or user already exists")
    })
    public ResponseEntity<Void> addNewUser(
            @Parameter(description = "User registration details", required = true) @RequestBody RegisterRequest registerRequest) {
        userService.createUser(registerRequest);
        return ResponseEntity.ok().build();
    }

    /**
     * Authenticates a user and generates a JWT token.
     *
     * @param loginRequest the user login credentials
     * @return ResponseEntity containing JWT token
     */
    @PostMapping("/login")
    @Operation(summary = "Authenticate user", description = "Authenticates user credentials and returns JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authentication successful - returns JWT token"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<LoginResponse> authenticateUser(
            @Parameter(description = "User login credentials", required = true) @RequestBody LoginRequest loginRequest) {
        User userDetails = userService.authenticate(loginRequest);
        return ResponseEntity.ok(new LoginResponse(jwtService.generateToken(userDetails)));
    }

}
