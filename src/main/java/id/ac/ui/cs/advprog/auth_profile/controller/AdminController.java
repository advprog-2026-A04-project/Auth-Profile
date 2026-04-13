package id.ac.ui.cs.advprog.auth_profile.controller;

import id.ac.ui.cs.advprog.auth_profile.dto.AdminUserResponse;
import id.ac.ui.cs.advprog.auth_profile.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<Page<AdminUserResponse>> getAllUsers(Pageable pageable) {
        Page<AdminUserResponse> users = adminService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    @PutMapping("/users/{id}/kyc")
    public ResponseEntity<AdminUserResponse> handleKyc(
            @PathVariable Long id,
            @RequestParam String action) {

        AdminUserResponse response;
        if ("APPROVE".equalsIgnoreCase(action)) {
            response = adminService.approveKyc(id);
        } else if ("REJECT".equalsIgnoreCase(action)) {
            response = adminService.rejectKyc(id);
        } else {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(response);
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<AdminUserResponse> updateRole(
            @PathVariable Long id,
            @RequestParam String role) {
        AdminUserResponse response = adminService.updateRole(id, role);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/users/{id}/ban")
    public ResponseEntity<AdminUserResponse> banUser(@PathVariable Long id) {
        AdminUserResponse response = adminService.banUser(id);
        return ResponseEntity.ok(response);
    }
}
