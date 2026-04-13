package id.ac.ui.cs.advprog.auth_profile.service;

import id.ac.ui.cs.advprog.auth_profile.dto.AdminUserResponse;
import id.ac.ui.cs.advprog.auth_profile.model.KycStatus;
import id.ac.ui.cs.advprog.auth_profile.model.User;
import id.ac.ui.cs.advprog.auth_profile.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;

    public Page<AdminUserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::toResponse);
    }

    public AdminUserResponse approveKyc(Long userId) {
        User user = findUserById(userId);

        if (user.getKycStatus() != KycStatus.PENDING) {
            throw new RuntimeException("KYC is not in PENDING status");
        }

        user.setKycStatus(KycStatus.APPROVED);
        user.setRole("JASTIPER");
        User saved = userRepository.save(user);
        return toResponse(saved);
    }

    public AdminUserResponse rejectKyc(Long userId) {
        User user = findUserById(userId);

        if (user.getKycStatus() != KycStatus.PENDING) {
            throw new RuntimeException("KYC is not in PENDING status");
        }

        user.setKycStatus(KycStatus.REJECTED);
        user.setRole("TITIPERS");
        User saved = userRepository.save(user);
        return toResponse(saved);
    }

    public AdminUserResponse updateRole(Long userId, String role) {
        User user = findUserById(userId);

        if (!"TITIPERS".equalsIgnoreCase(role)) {
            throw new RuntimeException("Can only demote to TITIPERS");
        }

        user.setRole("TITIPERS");
        user.setKycStatus(KycStatus.NONE);
        User saved = userRepository.save(user);
        return toResponse(saved);
    }

    public AdminUserResponse banUser(Long userId) {
        User user = findUserById(userId);

        if (user.isBanned()) {
            throw new RuntimeException("User is already banned");
        }

        user.setBanned(true);
        User saved = userRepository.save(user);
        return toResponse(saved);
    }

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private AdminUserResponse toResponse(User user) {
        return AdminUserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .role(user.getRole())
                .kycStatus(user.getKycStatus() != null ? user.getKycStatus().name() : "NONE")
                .banned(user.isBanned())
                .build();
    }
}
