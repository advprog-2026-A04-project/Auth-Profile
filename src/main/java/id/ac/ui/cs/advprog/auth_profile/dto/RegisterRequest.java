package id.ac.ui.cs.advprog.auth_profile.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String email;
    private String password;
    private String username;
}