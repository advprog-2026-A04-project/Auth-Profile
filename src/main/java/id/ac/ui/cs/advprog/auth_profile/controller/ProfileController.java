package id.ac.ui.cs.advprog.auth_profile.controller;

import id.ac.ui.cs.advprog.auth_profile.dto.ProfileResponse;
import id.ac.ui.cs.advprog.auth_profile.dto.ProfileUpdateRequest;
import id.ac.ui.cs.advprog.auth_profile.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/profile")
public class ProfileController {

    private final AuthService authService;

    public ProfileController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfileResponse> getProfile(@PathVariable("id") Long userId) {
        return ResponseEntity.ok(authService.getProfile(userId));
    }

    @PutMapping
    public ResponseEntity<ProfileResponse> updateProfile(
            Authentication authentication,
            @Valid @RequestBody ProfileUpdateRequest request
    ) {
        return ResponseEntity.ok(authService.updateProfile(Long.valueOf(authentication.getName()), request));
    }
}
