package id.ac.ui.cs.advprog.auth_profile.service;

import id.ac.ui.cs.advprog.auth_profile.model.KycStatus;
import id.ac.ui.cs.advprog.auth_profile.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private User testUser;
    private static final String SECRET = "mySecretKeyForJwtTokenGenerationThatIsLongEnoughForHS256Algorithm12345";
    private static final long EXPIRATION = 86400000L; // 24 hours

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, EXPIRATION);

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@test.com");
        testUser.setUsername("testuser");
        testUser.setRole("TITIPERS");
        testUser.setKycStatus(KycStatus.NONE);
    }

    @Test
    void generateToken_ReturnsValidToken() {
        String token = jwtService.generateToken(testUser);

        assertNotNull(token);
        assertFalse(token.isEmpty());

        // Verify the token can be parsed back
        Claims claims = jwtService.validateToken(token);
        assertEquals("test@test.com", claims.get("email", String.class));
        assertEquals("TITIPERS", claims.get("role", String.class));
        assertEquals(1L, claims.get("userId", Long.class));
    }

    @Test
    void validateToken_WithValidToken_ReturnsClaims() {
        String token = jwtService.generateToken(testUser);

        Claims claims = jwtService.validateToken(token);

        assertNotNull(claims);
        assertEquals("test@test.com", claims.getSubject());
        assertEquals("test@test.com", claims.get("email", String.class));
        assertEquals("TITIPERS", claims.get("role", String.class));
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
        assertTrue(claims.getExpiration().after(new Date()));
    }

    @Test
    void validateToken_WithExpiredToken_ThrowsException() {
        // Create a JwtService with 0ms expiration to force an expired token
        JwtService expiredJwtService = new JwtService(SECRET, 0);
        String token = expiredJwtService.generateToken(testUser);

        // Small delay to ensure expiration
        try { Thread.sleep(50); } catch (InterruptedException ignored) {}

        assertThrows(ExpiredJwtException.class,
                () -> expiredJwtService.validateToken(token));
    }

    @Test
    void getEmailFromToken_ReturnsCorrectEmail() {
        String token = jwtService.generateToken(testUser);

        String email = jwtService.getEmailFromToken(token);

        assertEquals("test@test.com", email);
    }
}
