package id.ac.ui.cs.advprog.auth_profile.controller;

import id.ac.ui.cs.advprog.auth_profile.dto.KycRequest;
import id.ac.ui.cs.advprog.auth_profile.dto.ProfileResponse;
import id.ac.ui.cs.advprog.auth_profile.dto.ProfileUpdateRequest;
import id.ac.ui.cs.advprog.auth_profile.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/{id}")
    public ResponseEntity<ProfileResponse> getProfile(@PathVariable Long id) {
        ProfileResponse profile = profileService.getProfile(id);
        return ResponseEntity.ok(profile);
    }

    @PutMapping
    public ResponseEntity<ProfileResponse> updateProfile(
            @RequestBody ProfileUpdateRequest request,
            Authentication authentication) {
        // Email is stored as the principal in SecurityContext by JwtAuthenticationFilter
        String email = authentication.getName();
        ProfileResponse profile = profileService.updateProfile(email, request);
        return ResponseEntity.ok(profile);
    }

    @PostMapping("/kyc")
    public ResponseEntity<ProfileResponse> submitKyc(
            @RequestBody KycRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        ProfileResponse profile = profileService.submitKyc(email, request);
        return ResponseEntity.ok(profile);
    }
}
