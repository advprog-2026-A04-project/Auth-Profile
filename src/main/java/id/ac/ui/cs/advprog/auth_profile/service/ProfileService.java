package id.ac.ui.cs.advprog.auth_profile.service;

import id.ac.ui.cs.advprog.auth_profile.dto.ProfileResponse;
import id.ac.ui.cs.advprog.auth_profile.dto.ProfileUpdateRequest;
import id.ac.ui.cs.advprog.auth_profile.model.User;
import id.ac.ui.cs.advprog.auth_profile.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;

    public ProfileResponse getProfile(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return buildProfileResponse(user);
    }

    public ProfileResponse updateProfile(String email, ProfileUpdateRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Validate username uniqueness if it's being changed
        if (request.getUsername() != null && !request.getUsername().isEmpty()) {
            if (!request.getUsername().equals(user.getUsername())
                    && userRepository.existsByUsername(request.getUsername())) {
                throw new RuntimeException("Username already taken");
            }
            user.setUsername(request.getUsername());
        }

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }

        User savedUser = userRepository.save(user);
        return buildProfileResponse(savedUser);
    }

    private ProfileResponse buildProfileResponse(User user) {
        ProfileResponse.ProfileResponseBuilder builder = ProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .role(user.getRole());

        if ("JASTIPER".equalsIgnoreCase(user.getRole())) {
            // Placeholder for Jastiper-specific details (e.g., ratings, trips, etc.)
            builder.jastiperDetails(null);
        } else {
            // TITIPERS — show basic info + verification status placeholder
            builder.verified(false); // placeholder, will be replaced with actual logic
        }

        return builder.build();
    }
}
