package id.ac.ui.cs.advprog.auth_profile.dto;

public record ProfileResponse(
        Long id,
        String email,
        String username,
        String fullName,
        String role
) {
}
