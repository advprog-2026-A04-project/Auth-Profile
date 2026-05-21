package id.ac.ui.cs.advprog.auth_profile.service;

import id.ac.ui.cs.advprog.auth_profile.dto.RegisterRequest;
import id.ac.ui.cs.advprog.auth_profile.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthUserFactoryTest {

    private final AuthUserFactory factory = new AuthUserFactory();

    @Test
    void createRegisteredUserShouldNormalizeFieldsAndEncodePassword() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail(" User@Example.COM ");
        request.setPassword(" Secret123! ");
        request.setUsername(" demo-user ");
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        when(encoder.encode("Secret123!")).thenReturn("encoded");

        User user = factory.createRegisteredUser(request, encoder, "demo-user");

        assertEquals("user@example.com", user.getEmail());
        assertEquals("demo-user", user.getUsername());
        assertEquals("demo-user", user.getFullName());
        assertEquals("encoded", user.getPassword());
        assertEquals("TITIPER", user.getRole());
    }

    @Test
    void normalizeHelpersShouldTrimEmailAndUsername() {
        assertEquals("person@example.com", factory.normalizeEmail(" Person@Example.com "));
        assertEquals("person", factory.normalizeUsername(" person "));
        assertEquals("", factory.normalizeUsername(null));
    }
}
