package id.ac.ui.cs.advprog.auth_profile.service;

import id.ac.ui.cs.advprog.auth_profile.config.JwtService;
import id.ac.ui.cs.advprog.auth_profile.dto.AuthResponse;
import id.ac.ui.cs.advprog.auth_profile.dto.LoginRequest;
import id.ac.ui.cs.advprog.auth_profile.dto.ProfileResponse;
import id.ac.ui.cs.advprog.auth_profile.dto.ProfileUpdateRequest;
import id.ac.ui.cs.advprog.auth_profile.dto.RegisterRequest;
import id.ac.ui.cs.advprog.auth_profile.model.User;
import id.ac.ui.cs.advprog.auth_profile.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        String username = request.getUsername().trim();

        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(CONFLICT, "Email is already registered.");
        }
        if (userRepository.existsByUsername(username)) {
            throw new ResponseStatusException(CONFLICT, "Username is already taken.");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword().trim()));
        user.setUsername(username);
        user.setFullName(username);
        user.setRole("TITIPER");

        return toAuthResponse(userRepository.save(user));
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Invalid email or password."));

        if (!passwordEncoder.matches(request.getPassword().trim(), user.getPassword())) {
            throw new ResponseStatusException(UNAUTHORIZED, "Invalid email or password.");
        }

        return toAuthResponse(user);
    }

    public ProfileResponse getCurrentProfile(Long userId) {
        return toProfileResponse(getUserById(userId));
    }

    public ProfileResponse getProfile(Long userId) {
        return toProfileResponse(getUserById(userId));
    }

    public ProfileResponse updateProfile(Long userId, ProfileUpdateRequest request) {
        User user = getUserById(userId);
        String username = request.username().trim();

        userRepository.findByUsername(username)
                .filter(existing -> !existing.getId().equals(userId))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(CONFLICT, "Username is already taken.");
                });

        user.setUsername(username);
        user.setFullName(request.fullName() == null || request.fullName().isBlank()
                ? username
                : request.fullName().trim());

        return toProfileResponse(userRepository.save(user));
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found."));
    }

    private AuthResponse toAuthResponse(User user) {
        return new AuthResponse(
                jwtService.generateToken(user),
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getFullName(),
                user.getRole()
        );
    }

    private ProfileResponse toProfileResponse(User user) {
        return new ProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getFullName(),
                user.getRole()
        );
    }
}
