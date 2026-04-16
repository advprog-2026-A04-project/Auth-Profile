package id.ac.ui.cs.advprog.auth_profile.config;

import id.ac.ui.cs.advprog.auth_profile.model.User;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class JwtServiceTest {

    @Test
    void generateAndParseTokenShouldRoundTripClaims() {
        JwtService jwtService = new JwtService("json-milestone-secret-json-milestone-secret", 3600);
        User user = new User();
        user.setId(15L);
        user.setEmail("user@example.com");
        user.setUsername("demo");
        user.setRole("TITIPER");

        String token = jwtService.generateToken(user);
        Claims claims = jwtService.parseToken(token);

        assertNotNull(token);
        assertEquals("15", claims.getSubject());
        assertEquals("user@example.com", claims.get("email", String.class));
        assertEquals("demo", claims.get("username", String.class));
        assertEquals("TITIPER", claims.get("role", String.class));
    }

    @Test
    void parseTokenShouldSupportBase64EncodedSecret() {
        JwtService jwtService = new JwtService("c29tZS1iYXNlNjQtc2VjcmV0LXN0cmluZy1mb3ItdGVzdGluZw==", 3600);
        User user = new User();
        user.setId(8L);
        user.setEmail("base64@example.com");
        user.setUsername("base64");
        user.setRole("ADMIN");

        Claims claims = jwtService.parseToken(jwtService.generateToken(user));

        assertEquals("8", claims.getSubject());
        assertEquals("ADMIN", claims.get("role", String.class));
    }
}
