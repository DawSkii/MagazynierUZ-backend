package org.example.magazynieruz.service;

import org.example.magazynieruz.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link JwtService}.
 * Verifies JWT token generation, validation, and claim extraction functionality.
 */
class JwtServiceTest {

    private JwtService jwtService;
    private String secretKey;
    private User testUser;

    @BeforeEach
    void setUp() {
        byte[] keyBytes = new byte[32];
        for (int i = 0; i < 32; i++) {
            keyBytes[i] = (byte) i;
        }
        secretKey = Base64.getEncoder().encodeToString(keyBytes);
        
        long jwtExpiration = 60;
        jwtService = new JwtService(secretKey, jwtExpiration);

        testUser = new User();
        testUser.setUsername("testuser");
    }

    /**
     * Tests JWT token generation and expects valid three-part token structure.
     */
    @Test
    void testGenerateToken_Success() {
        String token = jwtService.generateToken(testUser);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3);
    }

    /**
     * Tests JWT token generation with extra claims and expects valid token.
     */
    @Test
    void testGenerateToken_WithExtraClaims() {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", "ADMIN");
        extraClaims.put("orgId", 1L);

        String token = jwtService.generateToken(extraClaims, testUser);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    /**
     * Tests extracting username from JWT token and expects correct username.
     */
    @Test
    void testExtractUsername_Success() {
        String token = jwtService.generateToken(testUser);

        String username = jwtService.extractUsername(token);

        assertThat(username).isEqualTo("testuser");
    }

    /**
     * Tests token validation with valid token and matching user and expects true.
     */
    @Test
    void testIsTokenValid_ValidToken_ReturnsTrue() {
        String token = jwtService.generateToken(testUser);

        boolean isValid = jwtService.isTokenValid(token, testUser);

        assertThat(isValid).isTrue();
    }

    /**
     * Tests token validation with different user and expects false.
     */
    @Test
    void testIsTokenValid_DifferentUser_ReturnsFalse() {
        String token = jwtService.generateToken(testUser);
        
        User differentUser = new User();
        differentUser.setUsername("differentuser");

        boolean isValid = jwtService.isTokenValid(token, differentUser);

        assertThat(isValid).isFalse();
    }

    /**
     * Tests token validation with invalid token format and expects false.
     */
    @Test
    void testIsTokenValid_InvalidToken_ReturnsFalse() {
        String invalidToken = "invalid.jwt.token";

        boolean isValid = jwtService.isTokenValid(invalidToken, testUser);

        assertThat(isValid).isFalse();
    }

    /**
     * Tests retrieving expiration time and expects configured value.
     */
    @Test
    void testGetExpirationTimeInMinutes() {
        long expirationTime = jwtService.getExpirationTimeInMinutes();

        assertThat(expirationTime).isEqualTo(60);
    }

    /**
     * Tests extracting custom claim from token and expects correct subject.
     */
    @Test
    void testExtractClaim_CustomClaim() {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("customField", "customValue");
        String token = jwtService.generateToken(extraClaims, testUser);

        String subject = jwtService.extractClaim(token, claims -> claims.getSubject());

        assertThat(subject).isEqualTo("testuser");
    }

    /**
     * Tests that generated token contains correct username in subject.
     */
    @Test
    void testGeneratedToken_ContainsUsername() {
        testUser.setUsername("john.doe");

        String token = jwtService.generateToken(testUser);
        String extractedUsername = jwtService.extractUsername(token);

        assertThat(extractedUsername).isEqualTo("john.doe");
    }

    /**
     * Tests that different users generate different tokens with correct usernames.
     */
    @Test
    void testGenerateToken_DifferentUsers_GenerateDifferentTokens() {
        User user1 = new User();
        user1.setUsername("user1");
        
        User user2 = new User();
        user2.setUsername("user2");

        String token1 = jwtService.generateToken(user1);
        String token2 = jwtService.generateToken(user2);

        assertThat(token1).isNotEqualTo(token2);
        assertThat(jwtService.extractUsername(token1)).isEqualTo("user1");
        assertThat(jwtService.extractUsername(token2)).isEqualTo("user2");
    }
}
