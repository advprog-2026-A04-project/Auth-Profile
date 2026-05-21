package id.ac.ui.cs.advprog.auth_profile.dto;

import jakarta.validation.constraints.NotBlank;

public record SubmitKycRequest(
        @NotBlank(message = "KYC document URL is required.")
        String documentUrl,
        String note
) {
}
