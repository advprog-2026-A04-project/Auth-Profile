package id.ac.ui.cs.advprog.auth_profile.dto;

public record ProfileResponse(
        Long id,
        String email,
        String username,
        String fullName,
        String role,
        String kycStatus,
        boolean banned,
        int completedOrders,
        int ratingCount,
        double averageRating
) {
    public ProfileResponse(Long id, String email, String username, String fullName, String role) {
        this(id, email, username, fullName, role, "NOT_SUBMITTED", false);
    }

    public ProfileResponse(
            Long id,
            String email,
            String username,
            String fullName,
            String role,
            String kycStatus,
            boolean banned
    ) {
        this(id, email, username, fullName, role, kycStatus, banned, 0, 0, 0.0);
    }
}
