package org.example.magazynieruz.controller;

import org.example.magazynieruz.dto.auth.LoginRequest;
import org.example.magazynieruz.dto.auth.LoginResponse;
import org.example.magazynieruz.dto.auth.RegisterRequest;
import org.example.magazynieruz.model.User;
import org.example.magazynieruz.service.JwtService;
import org.example.magazynieruz.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link AuthController}.
 * Verifies authentication and user registration functionality.
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthController authController;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");
    }

    /**
     * Tests user registration with valid credentials and expects successful response.
     */
    @Test
    void testRegister_Success() {
        RegisterRequest registerRequest = new RegisterRequest("newuser", "password123");
        
        doNothing().when(userService).createUser(registerRequest);

        ResponseEntity<Void> response = authController.addNewUser(registerRequest);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        verify(userService).createUser(registerRequest);
    }

    /**
     * Tests user login with valid credentials and expects JWT token in response.
     */
    @Test
    void testLogin_Success() {
        LoginRequest loginRequest = new LoginRequest("testuser", "password123");
        String expectedToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token";
        
        when(userService.authenticate(loginRequest)).thenReturn(testUser);
        when(jwtService.generateToken(testUser)).thenReturn(expectedToken);

        ResponseEntity<LoginResponse> response = authController.authenticateUser(loginRequest);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().token()).isEqualTo(expectedToken);
        
        verify(userService).authenticate(loginRequest);
        verify(jwtService).generateToken(testUser);
    }

    /**
     * Tests the authentication flow verifies proper service interactions and token generation.
     */
    @Test
    void testLogin_VerifyAuthenticationFlow() {
        LoginRequest loginRequest = new LoginRequest("admin", "adminpass");
        String token = "generated.jwt.token";
        
        when(userService.authenticate(loginRequest)).thenReturn(testUser);
        when(jwtService.generateToken(testUser)).thenReturn(token);

        ResponseEntity<LoginResponse> response = authController.authenticateUser(loginRequest);

        assertThat(response.getBody().token()).isNotEmpty();
        verify(userService).authenticate(loginRequest);
        verify(jwtService).generateToken(any(User.class));
    }

    /**
     * Tests that successful login returns a valid JWT token with HTTP 200 status.
     */
    @Test
    void testLogin_ReturnsJwtToken() {
        LoginRequest request = new LoginRequest("user", "pass");
        String jwtToken = "valid.jwt.token.here";
        
        when(userService.authenticate(request)).thenReturn(testUser);
        when(jwtService.generateToken(testUser)).thenReturn(jwtToken);

        ResponseEntity<LoginResponse> response = authController.authenticateUser(request);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().token()).isEqualTo(jwtToken);
    }
}
