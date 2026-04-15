package id.ac.ui.cs.advprog.auth_profile.dto;

public record AuthResponse(
        String token,
        Long id,
        String email,
        String username,
        String fullName,
        String role
) {
}
