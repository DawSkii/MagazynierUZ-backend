package org.example.magazynieruz.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.example.magazynieruz.model.User;
import java.util.function.Function;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for JWT token generation and validation.
 * Handles JWT operations including token creation, parsing, and validation.
 */
@Service
public class JwtService {

    private final long jwtExpiration;
    private final SecretKey signInKey;

    public JwtService(
            @Value("${security.jwt.secret-key}") String secretKey,
            @Value("${security.jwt.expiration}") long jwtExpiration) {
        this.jwtExpiration = jwtExpiration;
        this.signInKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }

    /**
     * Extracts username from JWT token.
     *
     * @param token the JWT token
     * @return username from token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts specific claim from JWT token.
     *
     * @param token the JWT token
     * @param claimsResolver function to extract claim
     * @param <T> the claim type
     * @return extracted claim value
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token).getPayload();
        return claimsResolver.apply(claims);
    }

    /**
     * Generates JWT token for user.
     *
     * @param userDetails the user details
     * @return generated JWT token
     */
    public String generateToken(User userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Generates JWT token with additional claims.
     *
     * @param extraClaims additional claims to include
     * @param userDetails the user details
     * @return generated JWT token
     */
    public String generateToken(Map<String, Object> extraClaims, User userDetails) {
        return buildToken(extraClaims, userDetails, getExpirationTimeInMinutes());
    }

    /**
     * Gets token expiration time in minutes.
     *
     * @return expiration time in minutes
     */
    public long getExpirationTimeInMinutes() {
        return jwtExpiration;
    }

    /**
     * Validates JWT token against user details.
     *
     * @param token the JWT token
     * @param userDetails the user details to validate against
     * @return true if token is valid
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return username.equals(userDetails.getUsername());
        } catch (JwtException e) {
            return false;
        }
    }

    /**
     * Builds JWT token with claims and expiration.
     *
     * @param extraClaims additional claims
     * @param userDetails user details
     * @param expirationInMinutes expiration time
     * @return built JWT token
     */
    private String buildToken(
            Map<String, Object> extraClaims,
            User userDetails,
            long expirationInMinutes
    ) {
        return Jwts
                .builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plus(expirationInMinutes, ChronoUnit.MINUTES)))
                .signWith(signInKey)
                .compact();
    }

    /**
     * Extracts all claims from JWT token.
     *
     * @param token the JWT token
     * @return parsed claims
     */
    private Jws<Claims> extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signInKey)
                .build()
                .parseSignedClaims(token);
    }

}
