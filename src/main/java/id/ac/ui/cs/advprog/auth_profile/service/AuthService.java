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
            throw new ResponseStatusException(CONFLICT, "Email is already registered.");
        }
        if (userRepository.existsByUsername(username)) {
            throw new ResponseStatusException(CONFLICT, "Username is already taken.");
        }

        User user = authUserFactory.createRegisteredUser(request, passwordEncoder);
        return userProfileMapper.toAuthResponse(userRepository.save(user));
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(authUserFactory.normalizeEmail(request.getEmail()))
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Invalid email or password."));

        if (!passwordEncoder.matches(request.getPassword().trim(), user.getPassword())) {
            throw new ResponseStatusException(UNAUTHORIZED, "Invalid email or password.");
        }

        return userProfileMapper.toAuthResponse(user);
    }

    public ProfileResponse getCurrentProfile(Long userId) {
        return userProfileMapper.toProfileResponse(getUserById(userId));
    }

    public ProfileResponse getProfile(Long userId) {
        return userProfileMapper.toProfileResponse(getUserById(userId));
    }

    public ProfileResponse updateProfile(Long userId, ProfileUpdateRequest request) {
        User user = getUserById(userId);
        String username = authUserFactory.normalizeUsername(request.username());

        userRepository.findByUsername(username)
                .filter(existing -> !existing.getId().equals(userId))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(CONFLICT, "Username is already taken.");
                });

        user.setUsername(username);
        user.setFullName(request.fullName() == null || request.fullName().isBlank()
                ? username
                : request.fullName().trim());

        return userProfileMapper.toProfileResponse(userRepository.save(user));
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found."));
    }
}
