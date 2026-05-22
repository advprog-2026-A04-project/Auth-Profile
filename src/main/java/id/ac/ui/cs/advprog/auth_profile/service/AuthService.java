package id.ac.ui.cs.advprog.auth_profile.service;

import id.ac.ui.cs.advprog.auth_profile.config.JwtService;
import id.ac.ui.cs.advprog.auth_profile.dto.AuthResponse;
import id.ac.ui.cs.advprog.auth_profile.dto.LoginRequest;
import id.ac.ui.cs.advprog.auth_profile.dto.ProfileResponse;
import id.ac.ui.cs.advprog.auth_profile.dto.ProfileUpdateRequest;
import id.ac.ui.cs.advprog.auth_profile.dto.RegisterRequest;
import id.ac.ui.cs.advprog.auth_profile.dto.SubmitKycRequest;
import id.ac.ui.cs.advprog.auth_profile.model.User;
import id.ac.ui.cs.advprog.auth_profile.repository.UserRepository;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
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
        String username = resolveUsername(request.getUsername(), email);

        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(CONFLICT, "Email is already registered.");
        }
        if (hasRequestedUsername(request.getUsername()) && userRepository.existsByUsername(username)) {
            throw new ResponseStatusException(CONFLICT, "Username is already taken.");
        }

        User user = authUserFactory.createRegisteredUser(request, passwordEncoder, username);
        return userProfileMapper.toAuthResponse(userRepository.save(user));
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(authUserFactory.normalizeEmail(request.getEmail()))
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Invalid email or password."));

        if (!passwordEncoder.matches(request.getPassword().trim(), user.getPassword())) {
            throw new ResponseStatusException(UNAUTHORIZED, "Invalid email or password.");
        }
        if (user.isBanned()) {
            throw new ResponseStatusException(FORBIDDEN, "User is banned.");
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

    public ProfileResponse submitKyc(Long userId, SubmitKycRequest request) {
        User user = getUserById(userId);
        rejectBannedUser(user);

        user.setKycStatus("PENDING");
        user.setKycDocumentUrl(request.documentUrl().trim());
        user.setKycNote(request.note() == null || request.note().isBlank() ? null : request.note().trim());

        return userProfileMapper.toProfileResponse(userRepository.save(user));
    }

    public ProfileResponse approveKyc(Long userId, String note) {
        User user = getUserById(userId);
        user.setKycStatus("APPROVED");
        user.setKycNote(normalizeNote(note));
        user.setRole("JASTIPER");
        return userProfileMapper.toProfileResponse(userRepository.save(user));
    }

    public ProfileResponse rejectKyc(Long userId, String note) {
        User user = getUserById(userId);
        user.setKycStatus("REJECTED");
        user.setKycNote(normalizeNote(note));
        if ("JASTIPER".equals(user.getRole())) {
            user.setRole("TITIPER");
        }
        return userProfileMapper.toProfileResponse(userRepository.save(user));
    }

    public ProfileResponse banUser(Long userId, String note) {
        User user = getUserById(userId);
        user.setBanned(true);
        user.setKycNote(normalizeNote(note));
        return userProfileMapper.toProfileResponse(userRepository.save(user));
    }

    public ProfileResponse unbanUser(Long userId) {
        User user = getUserById(userId);
        user.setBanned(false);
        return userProfileMapper.toProfileResponse(userRepository.save(user));
    }

    public ProfileResponse demoteJastiper(Long userId, String note) {
        User user = getUserById(userId);
        user.setRole("TITIPER");
        user.setKycStatus("REJECTED");
        user.setKycNote(normalizeNote(note));
        return userProfileMapper.toProfileResponse(userRepository.save(user));
    }

    public List<ProfileResponse> listUsers() {
        return userRepository.findAll()
                .stream()
                .map(userProfileMapper::toProfileResponse)
                .toList();
    }

    public ProfileResponse recordJastiperCompletedOrder(Long userId) {
        User user = getUserById(userId);
        user.setCompletedOrders(user.getCompletedOrders() + 1);
        return userProfileMapper.toProfileResponse(userRepository.save(user));
    }

    public ProfileResponse recordJastiperRating(Long userId, Integer rating) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new ResponseStatusException(BAD_REQUEST, "Rating must be between 1 and 5.");
        }
        User user = getUserById(userId);
        user.setRatingCount(user.getRatingCount() + 1);
        user.setRatingTotal(user.getRatingTotal() + rating);
        return userProfileMapper.toProfileResponse(userRepository.save(user));
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found."));
    }

    private void rejectBannedUser(User user) {
        if (user.isBanned()) {
            throw new ResponseStatusException(FORBIDDEN, "User is banned.");
        }
    }

    private String normalizeNote(String note) {
        return note == null || note.isBlank() ? null : note.trim();
    }

    private String resolveUsername(String requestedUsername, String normalizedEmail) {
        if (hasRequestedUsername(requestedUsername)) {
            return authUserFactory.normalizeUsername(requestedUsername);
        }

        String emailLocalPart = normalizedEmail.split("@", 2)[0];
        String base = emailLocalPart
                .toLowerCase()
                .replaceAll("[^a-z0-9._-]", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
        if (base.isBlank()) {
            base = "titiper";
        }

        String candidate = base;
        int suffix = 2;
        while (userRepository.existsByUsername(candidate)) {
            candidate = "%s-%d".formatted(base, suffix);
            suffix++;
        }
        return candidate;
    }

    private boolean hasRequestedUsername(String username) {
        return username != null && !username.isBlank();
    }
}
