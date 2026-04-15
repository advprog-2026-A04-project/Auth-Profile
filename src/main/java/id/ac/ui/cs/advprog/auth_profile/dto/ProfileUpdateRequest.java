package id.ac.ui.cs.advprog.auth_profile.dto;

import jakarta.validation.constraints.NotBlank;

public record ProfileUpdateRequest(
        @NotBlank(message = "Username is required.")
        String username,
        String fullName
) {
}
