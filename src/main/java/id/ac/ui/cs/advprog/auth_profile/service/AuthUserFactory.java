package id.ac.ui.cs.advprog.auth_profile.service;

import id.ac.ui.cs.advprog.auth_profile.dto.RegisterRequest;
import id.ac.ui.cs.advprog.auth_profile.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AuthUserFactory {

    public User createRegisteredUser(RegisterRequest request, PasswordEncoder passwordEncoder) {
        User user = new User();
        user.setEmail(normalizeEmail(request.getEmail()));
        user.setUsername(normalizeUsername(request.getUsername()));
        user.setPassword(passwordEncoder.encode(request.getPassword().trim()));
        user.setFullName(user.getUsername());
        user.setRole("TITIPER");
        return user;
    }

    public String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    public String normalizeUsername(String username) {
        return username.trim();
    }
}
