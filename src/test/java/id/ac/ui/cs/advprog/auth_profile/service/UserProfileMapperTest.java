package id.ac.ui.cs.advprog.auth_profile.service;

import id.ac.ui.cs.advprog.auth_profile.config.JwtService;
import id.ac.ui.cs.advprog.auth_profile.dto.AuthResponse;
import id.ac.ui.cs.advprog.auth_profile.dto.ProfileResponse;
import id.ac.ui.cs.advprog.auth_profile.model.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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

    @Test
    void toProfileResponseShouldExposeReputationStatsAndAverageRating() {
        User user = sampleUser();
        user.setSuccessfulTransactionCount(4L);
        user.setJastiperRatingCount(2L);
        user.setJastiperRatingTotal(9L);
        UserProfileMapper mapper = new UserProfileMapper(mock(JwtService.class));

        ProfileResponse response = mapper.toProfileResponse(user);

        assertEquals(4L, response.successfulTransactionCount());
        assertEquals(4.5, response.averageJastiperRating());
    }

    @Test
    void toProfileResponseShouldDefaultMissingReputationStats() {
        User user = sampleUser();
        user.setSuccessfulTransactionCount(null);
        user.setJastiperRatingCount(null);
        user.setJastiperRatingTotal(null);
        UserProfileMapper mapper = new UserProfileMapper(mock(JwtService.class));

        ProfileResponse response = mapper.toProfileResponse(user);

        assertEquals(0L, response.successfulTransactionCount());
        assertNull(response.averageJastiperRating());
    }

    @Test
    void toProfileResponseShouldIgnoreRatingWhenTotalIsMissing() {
        User user = sampleUser();
        user.setJastiperRatingCount(2L);
        user.setJastiperRatingTotal(null);
        UserProfileMapper mapper = new UserProfileMapper(mock(JwtService.class));

        assertNull(mapper.toProfileResponse(user).averageJastiperRating());
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
