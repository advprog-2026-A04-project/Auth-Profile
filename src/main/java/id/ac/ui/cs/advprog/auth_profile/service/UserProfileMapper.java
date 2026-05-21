package id.ac.ui.cs.advprog.auth_profile.service;

import id.ac.ui.cs.advprog.auth_profile.config.JwtService;
import id.ac.ui.cs.advprog.auth_profile.dto.AuthResponse;
import id.ac.ui.cs.advprog.auth_profile.dto.ProfileResponse;
import id.ac.ui.cs.advprog.auth_profile.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserProfileMapper {

    private final JwtService jwtService;

    public UserProfileMapper(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public AuthResponse toAuthResponse(User user) {
        return new AuthResponse(
                jwtService.generateToken(user),
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getFullName(),
                user.getRole()
        );
    }

    public ProfileResponse toProfileResponse(User user) {
        return new ProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getFullName(),
                user.getRole(),
                user.getKycStatus(),
                user.isBanned(),
                user.getSuccessfulTransactionCount() == null ? 0L : user.getSuccessfulTransactionCount(),
                averageJastiperRating(user)
        );
    }

    private Double averageJastiperRating(User user) {
        Long ratingCount = user.getJastiperRatingCount();
        Long ratingTotal = user.getJastiperRatingTotal();
        if (ratingCount == null || ratingCount == 0 || ratingTotal == null) {
            return null;
        }
        return Math.round((ratingTotal.doubleValue() / ratingCount.doubleValue()) * 100.0) / 100.0;
    }
}
