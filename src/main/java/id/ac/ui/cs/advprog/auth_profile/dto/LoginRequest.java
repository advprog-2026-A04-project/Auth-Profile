package id.ac.ui.cs.advprog.auth_profile.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
}