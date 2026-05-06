package id.ac.ui.cs.advprog.auth_profile.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldSkipPublicEndpoints() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(mock(JwtService.class));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/auth/login");

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldSkipRegisterEndpoint() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(mock(JwtService.class));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/auth/register");

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldSkipHealthEndpoint() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(mock(JwtService.class));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/actuator/health");

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldPassThroughWithoutBearerToken() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(mock(JwtService.class));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/auth/me");

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldPassThroughWithNonBearerAuthorizationHeader() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(mock(JwtService.class));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/auth/me");
        request.addHeader("Authorization", "Basic abc123");

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldAuthenticateValidBearerToken() throws Exception {
        JwtService jwtService = new JwtService("json-milestone-secret-json-milestone-secret", 3600);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/auth/me");

        var user = new id.ac.ui.cs.advprog.auth_profile.model.User();
        user.setId(5L);
        user.setEmail("user@example.com");
        user.setUsername("demo");
        user.setRole("TITIPER");
        request.addHeader("Authorization", "Bearer " + jwtService.generateToken(user));

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("5", SecurityContextHolder.getContext().getAuthentication().getName());
        assertEquals("ROLE_TITIPER", SecurityContextHolder.getContext().getAuthentication().getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void shouldReturnUnauthorizedForInvalidBearerToken() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        when(jwtService.parseToken("broken")).thenThrow(new JwtException("bad token"));
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/auth/me");
        request.addHeader("Authorization", "Bearer broken");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertEquals(401, response.getStatus());
        var body = objectMapper.readTree(response.getContentAsString());
        assertEquals(401, body.get("status").asInt());
        assertEquals("Unauthorized", body.get("error").asText());
        assertEquals("Invalid or expired token.", body.get("message").asText());
        assertEquals("/auth/me", body.get("path").asText());
    }
}
