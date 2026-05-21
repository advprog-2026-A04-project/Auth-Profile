package id.ac.ui.cs.advprog.auth_profile.controller;

import id.ac.ui.cs.advprog.auth_profile.dto.AdminUserActionRequest;
import id.ac.ui.cs.advprog.auth_profile.dto.ProfileResponse;
import id.ac.ui.cs.advprog.auth_profile.dto.ProfileUpdateRequest;
import id.ac.ui.cs.advprog.auth_profile.dto.RecordJastiperRatingRequest;
import id.ac.ui.cs.advprog.auth_profile.dto.SubmitKycRequest;
import id.ac.ui.cs.advprog.auth_profile.service.AuthService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import static org.springframework.http.HttpStatus.FORBIDDEN;

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

    @PostMapping("/kyc")
    public ResponseEntity<ProfileResponse> submitKyc(
            Authentication authentication,
            @Valid @RequestBody SubmitKycRequest request
    ) {
        return ResponseEntity.ok(authService.submitKyc(Long.valueOf(authentication.getName()), request));
    }

    @GetMapping("/admin/users")
    public ResponseEntity<List<ProfileResponse>> listUsers(Authentication authentication) {
        requireAdmin(authentication);
        return ResponseEntity.ok(authService.listUsers());
    }

    @PostMapping("/admin/users/{id}/kyc/approve")
    public ResponseEntity<ProfileResponse> approveKyc(
            Authentication authentication,
            @PathVariable("id") Long userId,
            @RequestBody(required = false) AdminUserActionRequest request
    ) {
        requireAdmin(authentication);
        return ResponseEntity.ok(authService.approveKyc(userId, request == null ? null : request.note()));
    }

    @PostMapping("/admin/users/{id}/kyc/reject")
    public ResponseEntity<ProfileResponse> rejectKyc(
            Authentication authentication,
            @PathVariable("id") Long userId,
            @RequestBody(required = false) AdminUserActionRequest request
    ) {
        requireAdmin(authentication);
        return ResponseEntity.ok(authService.rejectKyc(userId, request == null ? null : request.note()));
    }

    @PostMapping("/admin/users/{id}/ban")
    public ResponseEntity<ProfileResponse> banUser(
            Authentication authentication,
            @PathVariable("id") Long userId,
            @RequestBody(required = false) AdminUserActionRequest request
    ) {
        requireAdmin(authentication);
        return ResponseEntity.ok(authService.banUser(userId, request == null ? null : request.note()));
    }

    @PostMapping("/admin/users/{id}/unban")
    public ResponseEntity<ProfileResponse> unbanUser(Authentication authentication, @PathVariable("id") Long userId) {
        requireAdmin(authentication);
        return ResponseEntity.ok(authService.unbanUser(userId));
    }

    @PostMapping("/admin/users/{id}/demote")
    public ResponseEntity<ProfileResponse> demoteJastiper(
            Authentication authentication,
            @PathVariable("id") Long userId,
            @RequestBody(required = false) AdminUserActionRequest request
    ) {
        requireAdmin(authentication);
        return ResponseEntity.ok(authService.demoteJastiper(userId, request == null ? null : request.note()));
    }

    @PostMapping("/internal/jastipers/{id}/completed-order")
    public ResponseEntity<ProfileResponse> recordJastiperCompletedOrder(
            Authentication authentication,
            @PathVariable("id") Long userId
    ) {
        requireInternal(authentication);
        return ResponseEntity.ok(authService.recordJastiperCompletedOrder(userId));
    }

    @PostMapping("/internal/jastipers/{id}/rating")
    public ResponseEntity<ProfileResponse> recordJastiperRating(
            Authentication authentication,
            @PathVariable("id") Long userId,
            @Valid @RequestBody RecordJastiperRatingRequest request
    ) {
        requireInternal(authentication);
        return ResponseEntity.ok(authService.recordJastiperRating(userId, request.rating()));
    }

    private void requireAdmin(Authentication authentication) {
        boolean admin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        if (!admin) {
            throw new ResponseStatusException(FORBIDDEN, "Admin role is required.");
        }
    }

    private void requireInternal(Authentication authentication) {
        boolean internal = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_INTERNAL".equals(authority.getAuthority()));
        if (!internal) {
            throw new ResponseStatusException(FORBIDDEN, "Internal service token is required.");
        }
    }
}
