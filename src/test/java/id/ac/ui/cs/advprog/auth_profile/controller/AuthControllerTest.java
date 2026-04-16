package id.ac.ui.cs.advprog.auth_profile.controller;

import id.ac.ui.cs.advprog.auth_profile.dto.AuthResponse;
import id.ac.ui.cs.advprog.auth_profile.dto.LoginRequest;
import id.ac.ui.cs.advprog.auth_profile.dto.ProfileResponse;
import id.ac.ui.cs.advprog.auth_profile.dto.RegisterRequest;
import id.ac.ui.cs.advprog.auth_profile.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthControllerTest {

    @Test
    void registerShouldReturnCreatedResponse() {
        AuthService service = mock(AuthService.class);
        AuthResponse response = new AuthResponse("jwt", 1L, "user@example.com", "demo", "Demo", "TITIPER");
        RegisterRequest request = new RegisterRequest();
        request.setEmail("user@example.com");
        request.setPassword("Password123!");
        request.setUsername("demo");
        when(service.register(request)).thenReturn(response);

        var entity = new AuthController(service).register(request);

        assertEquals(HttpStatus.CREATED, entity.getStatusCode());
        assertEquals(response, entity.getBody());
    }

    @Test
    void loginShouldReturnOkResponse() {
        AuthService service = mock(AuthService.class);
        AuthResponse response = new AuthResponse("jwt", 1L, "user@example.com", "demo", "Demo", "TITIPER");
        LoginRequest request = new LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("Password123!");
        when(service.login(request)).thenReturn(response);

        var entity = new AuthController(service).login(request);

        assertEquals(HttpStatus.OK, entity.getStatusCode());
        assertEquals(response, entity.getBody());
    }

    @Test
    void meShouldUseAuthenticatedUserId() {
        AuthService service = mock(AuthService.class);
        ProfileResponse response = new ProfileResponse(1L, "user@example.com", "demo", "Demo", "TITIPER");
        when(service.getCurrentProfile(1L)).thenReturn(response);

        var entity = new AuthController(service).me(new UsernamePasswordAuthenticationToken("1", null));

        assertEquals(response, entity.getBody());
    }
}
