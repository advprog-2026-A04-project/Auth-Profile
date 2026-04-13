package id.ac.ui.cs.advprog.auth_profile.service;

import id.ac.ui.cs.advprog.auth_profile.dto.KycRequest;
import id.ac.ui.cs.advprog.auth_profile.dto.ProfileResponse;
import id.ac.ui.cs.advprog.auth_profile.dto.ProfileUpdateRequest;
import id.ac.ui.cs.advprog.auth_profile.model.KycStatus;
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

    public ProfileResponse submitKyc(String email, KycRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!"TITIPERS".equalsIgnoreCase(user.getRole())) {
            throw new RuntimeException("Only TITIPERS can submit KYC");
        }

        if (KycStatus.PENDING.equals(user.getKycStatus())) {
            throw new RuntimeException("KYC application is already pending");
        }

        if (request.getKycFullName() == null || request.getKycFullName().isBlank()) {
            throw new RuntimeException("KYC full name is required");
        }

        if (request.getNik() == null || !request.getNik().matches("\\d{16}")) {
            throw new RuntimeException("NIK must be exactly 16 digits");
        }

        if (request.getPhoneNumber() == null || !request.getPhoneNumber().matches("^08\\d{8,11}$")) {
            throw new RuntimeException("Phone number must start with 08 and be 10-13 digits");
        }

        user.setKycFullName(request.getKycFullName());
        user.setKycNik(request.getNik());
        user.setKycPhoneNumber(request.getPhoneNumber());
        user.setKycStatus(KycStatus.PENDING);

        User savedUser = userRepository.save(user);
        return buildProfileResponse(savedUser);
    }

    private ProfileResponse buildProfileResponse(User user) {
        ProfileResponse.ProfileResponseBuilder builder = ProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .role(user.getRole())
                .kycStatus(user.getKycStatus() != null ? user.getKycStatus().name() : "NONE");

        if ("JASTIPER".equalsIgnoreCase(user.getRole())) {
            builder.jastiperDetails(null);
        } else {
            builder.verified(false);
        }

        return builder.build();
    }
}
