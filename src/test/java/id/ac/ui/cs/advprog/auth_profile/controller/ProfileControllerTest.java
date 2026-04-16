package id.ac.ui.cs.advprog.auth_profile.controller;

import id.ac.ui.cs.advprog.auth_profile.dto.ProfileResponse;
import id.ac.ui.cs.advprog.auth_profile.dto.ProfileUpdateRequest;
import id.ac.ui.cs.advprog.auth_profile.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProfileControllerTest {

    @Test
    void getProfileShouldDelegateToService() {
        AuthService service = mock(AuthService.class);
        ProfileResponse response = new ProfileResponse(7L, "user@example.com", "demo", "Demo", "TITIPER");
        when(service.getProfile(7L)).thenReturn(response);

        var entity = new ProfileController(service).getProfile(7L);

        assertEquals(response, entity.getBody());
    }

    @Test
    void updateProfileShouldUseAuthenticatedUserId() {
        AuthService service = mock(AuthService.class);
        ProfileUpdateRequest request = new ProfileUpdateRequest("demo", "Demo");
        ProfileResponse response = new ProfileResponse(7L, "user@example.com", "demo", "Demo", "TITIPER");
        when(service.updateProfile(7L, request)).thenReturn(response);

        var entity = new ProfileController(service)
                .updateProfile(new UsernamePasswordAuthenticationToken("7", null), request);

        assertEquals(response, entity.getBody());
    }
}
