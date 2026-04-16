package id.ac.ui.cs.advprog.auth_profile.service;

import id.ac.ui.cs.advprog.auth_profile.config.JwtService;
import id.ac.ui.cs.advprog.auth_profile.dto.AuthResponse;
import id.ac.ui.cs.advprog.auth_profile.dto.ProfileResponse;
import id.ac.ui.cs.advprog.auth_profile.model.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserProfileMapperTest {

    @Test
    void toAuthResponseShouldIncludeJwtAndUserFields() {
        JwtService jwtService = mock(JwtService.class);
        User user = sampleUser();
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        UserProfileMapper mapper = new UserProfileMapper(jwtService);
        AuthResponse response = mapper.toAuthResponse(user);

        assertEquals("jwt-token", response.token());
        assertEquals(7L, response.id());
        assertEquals("user@example.com", response.email());
        assertEquals("demo", response.username());
        assertEquals("Demo User", response.fullName());
        assertEquals("TITIPER", response.role());
    }

    @Test
    void toProfileResponseShouldMapProfileFields() {
        UserProfileMapper mapper = new UserProfileMapper(mock(JwtService.class));
        ProfileResponse response = mapper.toProfileResponse(sampleUser());

        assertEquals(7L, response.id());
        assertEquals("user@example.com", response.email());
        assertEquals("demo", response.username());
        assertEquals("Demo User", response.fullName());
        assertEquals("TITIPER", response.role());
    }

    private static User sampleUser() {
        User user = new User();
        user.setId(7L);
        user.setEmail("user@example.com");
        user.setUsername("demo");
        user.setFullName("Demo User");
        user.setRole("TITIPER");
        return user;
    }
}
