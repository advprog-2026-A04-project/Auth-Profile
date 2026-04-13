package id.ac.ui.cs.advprog.auth_profile.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.auth_profile.dto.LoginRequest;
import id.ac.ui.cs.advprog.auth_profile.dto.RegisterRequest;
import id.ac.ui.cs.advprog.auth_profile.model.User;
import id.ac.ui.cs.advprog.auth_profile.repository.UserRepository;
import id.ac.ui.cs.advprog.auth_profile.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void register_Success_ReturnsTokenAndEmail() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("newuser@test.com");
        request.setPassword("password123");
        request.setUsername("newuser");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.email", is("newuser@test.com")));
    }

    @Test
    void login_Success_ReturnsTokenAndEmail() throws Exception {
        // Seed a user first
        User user = new User();
        user.setEmail("login@test.com");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setUsername("loginuser");
        userRepository.save(user);

        LoginRequest request = new LoginRequest();
        request.setEmail("login@test.com");
        request.setPassword("password123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.email", is("login@test.com")));
    }

    @Test
    void getMe_WithValidToken_ReturnsUserData() throws Exception {
        // Seed a user
        User user = new User();
        user.setEmail("me@test.com");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setUsername("meuser");
        User savedUser = userRepository.save(user);

        // Generate a valid token
        String token = jwtService.generateToken(savedUser);

        mockMvc.perform(get("/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("me@test.com")))
                .andExpect(jsonPath("$.username", is("meuser")))
                .andExpect(jsonPath("$.role", is("TITIPERS")));
    }

    @Test
    void getMe_WithoutToken_ReturnsUnauthorized() throws Exception {
        // /auth/me is under /auth/** which is permitAll, so Spring Security won't block it.
        // The controller checks authentication == null and returns 401 manually.
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized());
    }
}
