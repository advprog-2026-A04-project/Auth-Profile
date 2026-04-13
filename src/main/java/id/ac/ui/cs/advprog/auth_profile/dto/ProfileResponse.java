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

    private Object jastiperDetails;
    private Boolean verified;
}
