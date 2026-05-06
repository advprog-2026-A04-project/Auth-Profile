package id.ac.ui.cs.advprog.auth_profile.service;

import id.ac.ui.cs.advprog.auth_profile.dto.AuthResponse;
import id.ac.ui.cs.advprog.auth_profile.dto.LoginRequest;
import id.ac.ui.cs.advprog.auth_profile.dto.ProfileResponse;
import id.ac.ui.cs.advprog.auth_profile.dto.ProfileUpdateRequest;
import id.ac.ui.cs.advprog.auth_profile.dto.RegisterRequest;
import id.ac.ui.cs.advprog.auth_profile.model.User;
import id.ac.ui.cs.advprog.auth_profile.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
public class AuthService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthUserFactory authUserFactory;
    private final UserProfileMapper userProfileMapper;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthUserFactory authUserFactory,
            UserProfileMapper userProfileMapper
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authUserFactory = authUserFactory;
        this.userProfileMapper = userProfileMapper;
    }

    public AuthResponse register(RegisterRequest request) {
        String email = authUserFactory.normalizeEmail(request.getEmail());
        String username = authUserFactory.normalizeUsername(request.getUsername());

        if (userRepository.existsByEmail(email)) {
            LOGGER.warn("Registration rejected: duplicate email {}", maskEmail(email));
            throw new ResponseStatusException(CONFLICT, "Email is already registered.");
        }
        if (userRepository.existsByUsername(username)) {
            LOGGER.warn("Registration rejected: duplicate username {}", username);
            throw new ResponseStatusException(CONFLICT, "Username is already taken.");
        }

        User user = authUserFactory.createRegisteredUser(request, passwordEncoder);
        User savedUser = userRepository.save(user);
        LOGGER.info("User registered successfully: userId={}, role={}", savedUser.getId(), savedUser.getRole());
        return userProfileMapper.toAuthResponse(savedUser);
    }

    public AuthResponse login(LoginRequest request) {
        String email = authUserFactory.normalizeEmail(request.getEmail());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    LOGGER.warn("Login rejected: unknown email {}", maskEmail(email));
                    return new ResponseStatusException(UNAUTHORIZED, "Invalid email or password.");
                });

        if (!passwordEncoder.matches(request.getPassword().trim(), user.getPassword())) {
            LOGGER.warn("Login rejected: invalid password for userId={}", user.getId());
            throw new ResponseStatusException(UNAUTHORIZED, "Invalid email or password.");
        }

        LOGGER.info("User logged in successfully: userId={}, role={}", user.getId(), user.getRole());
        return userProfileMapper.toAuthResponse(user);
    }

    public ProfileResponse getCurrentProfile(Long userId) {
        LOGGER.debug("Current profile requested: userId={}", userId);
        return userProfileMapper.toProfileResponse(getUserById(userId));
    }

    public ProfileResponse getProfile(Long userId) {
        LOGGER.debug("Profile requested: userId={}", userId);
        return userProfileMapper.toProfileResponse(getUserById(userId));
    }

    public ProfileResponse updateProfile(Long userId, ProfileUpdateRequest request) {
        User user = getUserById(userId);
        String username = authUserFactory.normalizeUsername(request.username());

        userRepository.findByUsername(username)
                .filter(existing -> !existing.getId().equals(userId))
                .ifPresent(existing -> {
                    LOGGER.warn("Profile update rejected: username conflict for userId={}", userId);
                    throw new ResponseStatusException(CONFLICT, "Username is already taken.");
                });

        user.setUsername(username);
        user.setFullName(request.fullName() == null || request.fullName().isBlank()
                ? username
                : request.fullName().trim());

        User savedUser = userRepository.save(user);
        LOGGER.info("Profile updated successfully: userId={}", savedUser.getId());
        return userProfileMapper.toProfileResponse(savedUser);
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    LOGGER.warn("User lookup failed: userId={} not found", userId);
                    return new ResponseStatusException(NOT_FOUND, "User not found.");
                });
    }

    private String maskEmail(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex <= 0 || atIndex == email.length() - 1) {
            return "***";
        }

        return email.charAt(0) + "***@" + email.substring(atIndex + 1);
    }
}
