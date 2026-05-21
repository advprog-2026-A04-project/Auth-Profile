package id.ac.ui.cs.advprog.auth_profile.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class InternalTokenAuthenticationFilterTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldAuthenticateMatchingInternalToken() throws Exception {
        InternalTokenAuthenticationFilter filter = new InternalTokenAuthenticationFilter("secret");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Internal-Token", "secret");

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertEquals("internal-service", authentication.getName());
        assertEquals("ROLE_INTERNAL", authentication.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void shouldPassThroughWhenInternalTokenHeaderIsMissing() throws Exception {
        InternalTokenAuthenticationFilter filter = new InternalTokenAuthenticationFilter("secret");

        filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), new MockFilterChain());

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldPassThroughWhenInternalTokenDoesNotMatch() throws Exception {
        InternalTokenAuthenticationFilter filter = new InternalTokenAuthenticationFilter("secret");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Internal-Token", "wrong");

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldSkipWhenAuthenticationAlreadyExists() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("7", null)
        );
        InternalTokenAuthenticationFilter filter = new InternalTokenAuthenticationFilter("secret");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Internal-Token", "secret");

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertEquals("7", SecurityContextHolder.getContext().getAuthentication().getName());
    }
}
