package id.ac.ui.cs.advprog.auth_profile.dto;

public record ProfileResponse(
        Long id,
        String email,
        String username,
        String fullName,
        String role,
        String kycStatus,
        boolean banned,
        Long successfulTransactionCount,
        Double averageJastiperRating
) {
    public ProfileResponse(Long id, String email, String username, String fullName, String role) {
        this(id, email, username, fullName, role, "NOT_SUBMITTED", false, 0L, null);
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
        this(id, email, username, fullName, role, kycStatus, banned, 0L, null);
    }
}
