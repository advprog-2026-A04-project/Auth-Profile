package id.ac.ui.cs.advprog.auth_profile.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityConfigTest {

    @Test
    void passwordEncoderShouldEncodeAndMatchPasswords() {
        SecurityConfig config = new SecurityConfig();
        PasswordEncoder encoder = config.passwordEncoder();

        String encoded = encoder.encode("Password123!");

        assertTrue(encoder.matches("Password123!", encoded));
    }

    @Test
    void corsConfigurationShouldSplitAllowedOrigins() {
        SecurityConfig config = new SecurityConfig();
        ReflectionTestUtils.setField(config, "allowedOrigins", "http://localhost:5173,https://example.com");

        CorsConfigurationSource source = config.corsConfigurationSource();
        CorsConfiguration corsConfiguration = ((UrlBasedCorsConfigurationSource) source)
                .getCorsConfigurations()
                .get("/**");

        assertEquals(2, corsConfiguration.getAllowedOrigins().size());
        assertTrue(corsConfiguration.getAllowedMethods().contains("PUT"));
        assertFalse(Boolean.TRUE.equals(corsConfiguration.getAllowCredentials()));
    }
}
