package id.ac.ui.cs.advprog.auth_profile.dto;

public record ProfileResponse(
        Long id,
        String email,
        String username,
        String fullName,
        String role,
        String kycStatus,
        boolean banned
) {
    public ProfileResponse(Long id, String email, String username, String fullName, String role) {
        this(id, email, username, fullName, role, "NOT_SUBMITTED", false);
    }
}
