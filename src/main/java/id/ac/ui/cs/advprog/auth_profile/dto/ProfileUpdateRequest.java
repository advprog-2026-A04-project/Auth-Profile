package id.ac.ui.cs.advprog.auth_profile.dto;

import lombok.Data;

@Data
public class ProfileUpdateRequest {
    private String username;
    private String fullName;
}
