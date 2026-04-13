package id.ac.ui.cs.advprog.auth_profile.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {
    private Long id;
    private String email;
    private String username;
    private String fullName;
    private String role;

    private String kycStatus;

    // Placeholder fields for Jastiper role
    private Object jastiperDetails;

    // Placeholder field for Titipers role
    private Boolean verified;
}
