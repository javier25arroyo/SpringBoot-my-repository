package com.project.demo.logic.entity.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    private String secretKey = "testSecretKeytestSecretKeytestSecretKeytestSecretKeytestSecretKey"; // Must be long enough for HS256
    private long jwtExpiration = 3600000; // 1 hour

    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtService, "secretKey", secretKey);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", jwtExpiration);
        userDetails = new User("testuser", "password", new ArrayList<>());
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Test
    void extractUsername() {
        String token = Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
        String username = jwtService.extractUsername(token);
        assertEquals(userDetails.getUsername(), username);
    }

    @Test
    void extractClaim() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("customClaim", "customValue");
        String token = Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();

        String customClaimValue = jwtService.extractClaim(token, (Claims c) -> c.get("customClaim", String.class));
        assertEquals("customValue", customClaimValue);
    }

    @Test
    void generateToken() {
        String token = jwtService.generateToken(userDetails);
        assertNotNull(token);
        assertEquals(userDetails.getUsername(), jwtService.extractUsername(token));
    }

    @Test
    void generateTokenWithExtraClaims() {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", 123);
        String token = jwtService.generateToken(extraClaims, userDetails);
        assertNotNull(token);
        assertEquals(userDetails.getUsername(), jwtService.extractUsername(token));
        assertEquals(123, jwtService.extractClaim(token, (Claims c) -> c.get("userId", Integer.class)));
    }

    @Test
    void isTokenValid() {
        String token = jwtService.generateToken(userDetails);
        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void isTokenValid_invalidUsername() {
        String token = jwtService.generateToken(userDetails);
        UserDetails otherUserDetails = new User("otheruser", "password", new ArrayList<>());
        assertFalse(jwtService.isTokenValid(token, otherUserDetails));
    }

    @Test
    void isTokenExpired() {
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", -jwtExpiration); // Set expiration to past
        String token = jwtService.generateToken(userDetails);
        // When a token is expired, extractUsername itself will throw an ExpiredJwtException
        // So, isTokenValid should effectively be false, or rather, throw an exception.
        // Let's test that extractUsername throws the expected exception.
        assertThrows(io.jsonwebtoken.ExpiredJwtException.class, () -> {
            jwtService.extractUsername(token);
        });

        // Consequently, isTokenValid should also fail or throw an exception if it tries to extract username.
        // We can assert that isTokenValid returns false by catching the exception it might throw.
        try {
            assertFalse(jwtService.isTokenValid(token, userDetails));
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            // This is also an acceptable outcome, as the token validation fails due to expiration.
            assertTrue(true, "ExpiredJwtException was thrown as expected during isTokenValid check.");
        }
    }

    @Test
    void getExpirationTime() {
        assertEquals(jwtExpiration, jwtService.getExpirationTime());
    }
}
