package id.ac.ui.cs.advprog.auth_profile.service;

import id.ac.ui.cs.advprog.auth_profile.dto.AuthResponse;
import id.ac.ui.cs.advprog.auth_profile.dto.LoginRequest;
import id.ac.ui.cs.advprog.auth_profile.dto.ProfileResponse;
import id.ac.ui.cs.advprog.auth_profile.dto.ProfileUpdateRequest;
import id.ac.ui.cs.advprog.auth_profile.dto.RegisterRequest;
import id.ac.ui.cs.advprog.auth_profile.dto.SubmitKycRequest;
import id.ac.ui.cs.advprog.auth_profile.model.User;
import id.ac.ui.cs.advprog.auth_profile.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private AuthUserFactory authUserFactory;
    private UserProfileMapper userProfileMapper;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        authUserFactory = new AuthUserFactory();
        userProfileMapper = mock(UserProfileMapper.class);
        authService = new AuthService(userRepository, passwordEncoder, authUserFactory, userProfileMapper);
    }

    @Test
    void registerShouldRejectDuplicateEmail() {
        RegisterRequest request = registerRequest("user@example.com", "Password123!", "demo");
        when(userRepository.existsByEmail("user@example.com")).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> authService.register(request));

        assertEquals(409, exception.getStatusCode().value());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerShouldRejectDuplicateUsername() {
        RegisterRequest request = registerRequest("user@example.com", "Password123!", "demo");
        when(userRepository.existsByEmail("user@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("demo")).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> authService.register(request));

        assertEquals(409, exception.getStatusCode().value());
    }

    @Test
    void registerShouldPersistNormalizedUser() {
        RegisterRequest request = registerRequest(" USER@EXAMPLE.COM ", " Password123! ", " demo ");
        User savedUser = sampleUser();
        AuthResponse mapped = new AuthResponse("jwt", 1L, "user@example.com", "demo", "demo", "TITIPER");

        when(userRepository.existsByEmail("user@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("demo")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userProfileMapper.toAuthResponse(savedUser)).thenReturn(mapped);

        AuthResponse response = authService.register(request);

        assertEquals("jwt", response.token());
        verify(userRepository).save(any(User.class));
        verify(userProfileMapper).toAuthResponse(savedUser);
    }

    @Test
    void registerShouldGenerateUniqueUsernameWhenRequestOmitsUsername() {
        RegisterRequest request = registerRequest(" Fresh.User@Example.COM ", " Password123! ", "   ");
        User savedUser = sampleUser();
        savedUser.setUsername("fresh.user-2");
        AuthResponse mapped = new AuthResponse("jwt", 1L, "fresh.user@example.com", "fresh.user-2", "fresh.user-2", "TITIPER");

        when(userRepository.existsByEmail("fresh.user@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("fresh.user")).thenReturn(true);
        when(userRepository.existsByUsername("fresh.user-2")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            assertEquals("fresh.user-2", user.getUsername());
            assertEquals("fresh.user-2", user.getFullName());
            return savedUser;
        });
        when(userProfileMapper.toAuthResponse(savedUser)).thenReturn(mapped);

        AuthResponse response = authService.register(request);

        assertEquals("fresh.user-2", response.username());
    }

    @Test
    void registerShouldGenerateFallbackUsernameWhenEmailLocalPartHasNoUsableCharacters() {
        RegisterRequest request = registerRequest(" !!!@example.com ", " Password123! ", null);
        User savedUser = sampleUser();
        savedUser.setUsername("titiper");
        AuthResponse mapped = new AuthResponse("jwt", 1L, "!!!@example.com", "titiper", "titiper", "TITIPER");

        when(userRepository.existsByEmail("!!!@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("titiper")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            assertEquals("titiper", user.getUsername());
            return savedUser;
        });
        when(userProfileMapper.toAuthResponse(savedUser)).thenReturn(mapped);

        assertEquals("titiper", authService.register(request).username());
    }

    @Test
    void loginShouldRejectUnknownEmail() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.login(loginRequest("missing@example.com", "Password123!"))
        );

        assertEquals(401, exception.getStatusCode().value());
    }

    @Test
    void loginShouldRejectWrongPassword() {
        User user = sampleUser();
        user.setPassword("encoded");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password123!", "encoded")).thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.login(loginRequest("user@example.com", "Password123!"))
        );

        assertEquals(401, exception.getStatusCode().value());
    }

    @Test
    void loginShouldReturnMappedResponseForValidCredentials() {
        User user = sampleUser();
        user.setPassword("encoded");
        AuthResponse mapped = new AuthResponse("jwt", 1L, "user@example.com", "demo", "Demo User", "TITIPER");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password123!", "encoded")).thenReturn(true);
        when(userProfileMapper.toAuthResponse(user)).thenReturn(mapped);

        AuthResponse response = authService.login(loginRequest(" user@example.com ", " Password123! "));

        assertEquals("jwt", response.token());
        verify(userProfileMapper).toAuthResponse(user);
    }

    @Test
    void loginShouldRejectBannedUser() {
        User user = sampleUser();
        user.setPassword("encoded");
        user.setBanned(true);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password123!", "encoded")).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.login(loginRequest("user@example.com", "Password123!"))
        );

        assertEquals(403, exception.getStatusCode().value());
    }

    @Test
    void getCurrentProfileShouldLoadUserOrFail() {
        User user = sampleUser();
        ProfileResponse mapped = new ProfileResponse(1L, "user@example.com", "demo", "Demo User", "TITIPER");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userProfileMapper.toProfileResponse(user)).thenReturn(mapped);

        assertEquals(mapped, authService.getCurrentProfile(1L));
        verify(userProfileMapper).toProfileResponse(user);
    }

    @Test
    void getProfileShouldRejectMissingUser() {
        when(userRepository.findById(9L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.getProfile(9L)
        );

        assertEquals(404, exception.getStatusCode().value());
    }

    @Test
    void getProfileShouldReturnMappedProfileWhenPresent() {
        User user = sampleUser();
        ProfileResponse mapped = new ProfileResponse(1L, "user@example.com", "demo", "Demo User", "TITIPER");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userProfileMapper.toProfileResponse(user)).thenReturn(mapped);

        assertEquals(mapped, authService.getProfile(1L));
    }

    @Test
    void updateProfileShouldRejectTakenUsernameFromAnotherUser() {
        User user = sampleUser();
        User conflicting = sampleUser();
        conflicting.setId(2L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findByUsername("taken")).thenReturn(Optional.of(conflicting));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.updateProfile(1L, new ProfileUpdateRequest("taken", "Other"))
        );

        assertEquals(409, exception.getStatusCode().value());
    }

    @Test
    void updateProfileShouldUseUsernameAsFallbackFullName() {
        User user = sampleUser();
        ProfileResponse mapped = new ProfileResponse(1L, "user@example.com", "fresh", "fresh", "TITIPER");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findByUsername("fresh")).thenReturn(Optional.empty());
        when(userRepository.save(user)).thenReturn(user);
        when(userProfileMapper.toProfileResponse(user)).thenReturn(mapped);

        ProfileResponse response = authService.updateProfile(1L, new ProfileUpdateRequest(" fresh ", "   "));

        assertEquals("fresh", response.username());
        assertEquals("fresh", user.getFullName());
    }

    @Test
    void updateProfileShouldAllowSameUserToKeepUsernameAndTrimFullName() {
        User user = sampleUser();
        ProfileResponse mapped = new ProfileResponse(1L, "user@example.com", "demo", "Demo Person", "TITIPER");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findByUsername("demo")).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userProfileMapper.toProfileResponse(user)).thenReturn(mapped);

        ProfileResponse response = authService.updateProfile(1L, new ProfileUpdateRequest(" demo ", " Demo Person "));

        assertEquals("demo", response.username());
        assertEquals("Demo Person", user.getFullName());
    }

    @Test
    void updateProfileShouldUseUsernameWhenFullNameIsNull() {
        User user = sampleUser();
        ProfileResponse mapped = new ProfileResponse(1L, "user@example.com", "fresh", "fresh", "TITIPER");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findByUsername("fresh")).thenReturn(Optional.empty());
        when(userRepository.save(user)).thenReturn(user);
        when(userProfileMapper.toProfileResponse(user)).thenReturn(mapped);

        ProfileResponse response = authService.updateProfile(1L, new ProfileUpdateRequest(" fresh ", null));

        assertEquals("fresh", response.fullName());
        assertEquals("fresh", user.getFullName());
    }

    @Test
    void submitKycShouldMoveUserToPending() {
        User user = sampleUser();
        ProfileResponse mapped = new ProfileResponse(1L, "user@example.com", "demo", "Demo User", "TITIPER", "PENDING", false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userProfileMapper.toProfileResponse(user)).thenReturn(mapped);

        ProfileResponse response = authService.submitKyc(1L, new SubmitKycRequest(" https://docs.example/kyc.pdf ", " ready "));

        assertEquals("PENDING", user.getKycStatus());
        assertEquals("https://docs.example/kyc.pdf", user.getKycDocumentUrl());
        assertEquals("ready", user.getKycNote());
        assertEquals("PENDING", response.kycStatus());
    }

    @Test
    void submitKycShouldNormalizeMissingAndBlankNotesToNull() {
        User nullNoteUser = sampleUser();
        User blankNoteUser = sampleUser();
        when(userRepository.findById(1L)).thenReturn(Optional.of(nullNoteUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(blankNoteUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userProfileMapper.toProfileResponse(any(User.class)))
                .thenReturn(new ProfileResponse(1L, "user@example.com", "demo", "Demo User", "TITIPER", "PENDING", false));

        authService.submitKyc(1L, new SubmitKycRequest("https://docs.example/kyc.pdf", null));
        authService.submitKyc(2L, new SubmitKycRequest("https://docs.example/kyc.pdf", "   "));

        assertEquals(null, nullNoteUser.getKycNote());
        assertEquals(null, blankNoteUser.getKycNote());
    }

    @Test
    void submitKycShouldRejectBannedUserAndNormalizeEmptyNote() {
        User user = sampleUser();
        user.setBanned(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.submitKyc(1L, new SubmitKycRequest("https://docs.example/kyc.pdf", ""))
        );

        assertEquals(403, exception.getStatusCode().value());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void approveKycShouldPromoteUserToJastiper() {
        User user = sampleUser();
        ProfileResponse mapped = new ProfileResponse(1L, "user@example.com", "demo", "Demo User", "JASTIPER", "APPROVED", false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userProfileMapper.toProfileResponse(user)).thenReturn(mapped);

        ProfileResponse response = authService.approveKyc(1L, " valid document ");

        assertEquals("JASTIPER", user.getRole());
        assertEquals("APPROVED", user.getKycStatus());
        assertEquals("valid document", user.getKycNote());
        assertEquals("JASTIPER", response.role());
    }

    @Test
    void rejectKycShouldDemoteExistingJastiper() {
        User user = sampleUser();
        user.setRole("JASTIPER");
        ProfileResponse mapped = new ProfileResponse(1L, "user@example.com", "demo", "Demo User", "TITIPER", "REJECTED", false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userProfileMapper.toProfileResponse(user)).thenReturn(mapped);

        ProfileResponse response = authService.rejectKyc(1L, "invalid");

        assertEquals("TITIPER", user.getRole());
        assertEquals("REJECTED", response.kycStatus());
    }

    @Test
    void rejectKycShouldKeepTitiperRoleWhenUserWasNotJastiper() {
        User user = sampleUser();
        ProfileResponse mapped = new ProfileResponse(1L, "user@example.com", "demo", "Demo User", "TITIPER", "REJECTED", false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userProfileMapper.toProfileResponse(user)).thenReturn(mapped);

        ProfileResponse response = authService.rejectKyc(1L, "   ");

        assertEquals("TITIPER", response.role());
        assertEquals(null, user.getKycNote());
    }

    @Test
    void banUnbanAndDemoteShouldUpdateAdminControlledFields() {
        User user = sampleUser();
        user.setRole("JASTIPER");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userProfileMapper.toProfileResponse(user))
                .thenReturn(
                        new ProfileResponse(1L, "user@example.com", "demo", "Demo User", "JASTIPER", "NOT_SUBMITTED", true),
                        new ProfileResponse(1L, "user@example.com", "demo", "Demo User", "JASTIPER", "NOT_SUBMITTED", false),
                        new ProfileResponse(1L, "user@example.com", "demo", "Demo User", "TITIPER", "REJECTED", false)
                );

        assertEquals(true, authService.banUser(1L, "fraud").banned());
        assertEquals(false, authService.unbanUser(1L).banned());
        assertEquals("TITIPER", authService.demoteJastiper(1L, "policy").role());
        assertEquals("TITIPER", user.getRole());
    }

    @Test
    void listUsersShouldMapAllUsers() {
        User user = sampleUser();
        ProfileResponse mapped = new ProfileResponse(1L, "user@example.com", "demo", "Demo User", "TITIPER");
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(userProfileMapper.toProfileResponse(user)).thenReturn(mapped);

        List<ProfileResponse> users = authService.listUsers();

        assertEquals(1, users.size());
        assertEquals("demo", users.getFirst().username());
    }

    @Test
    void recordJastiperCompletedOrderShouldIncrementProfileStatistic() {
        User user = sampleUser();
        user.setRole("JASTIPER");
        user.setSuccessfulTransactionCount(2L);
        ProfileResponse mapped = new ProfileResponse(
                1L,
                "user@example.com",
                "demo",
                "Demo User",
                "JASTIPER",
                "APPROVED",
                false,
                3L,
                null
        );
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userProfileMapper.toProfileResponse(user)).thenReturn(mapped);

        ProfileResponse response = authService.recordJastiperCompletedOrder(1L);

        assertEquals(3L, user.getSuccessfulTransactionCount());
        assertEquals(3L, response.successfulTransactionCount());
    }

    @Test
    void recordJastiperCompletedOrderShouldHandleMissingCounter() {
        User user = sampleUser();
        user.setRole("JASTIPER");
        user.setSuccessfulTransactionCount(null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userProfileMapper.toProfileResponse(user)).thenReturn(new ProfileResponse(
                1L,
                "user@example.com",
                "demo",
                "Demo User",
                "JASTIPER",
                "APPROVED",
                false,
                1L,
                null
        ));

        ProfileResponse response = authService.recordJastiperCompletedOrder(1L);

        assertEquals(1L, user.getSuccessfulTransactionCount());
        assertEquals(1L, response.successfulTransactionCount());
    }

    @Test
    void recordJastiperRatingShouldUpdateAverageRatingInputs() {
        User user = sampleUser();
        user.setRole("JASTIPER");
        user.setJastiperRatingCount(1L);
        user.setJastiperRatingTotal(4L);
        ProfileResponse mapped = new ProfileResponse(
                1L,
                "user@example.com",
                "demo",
                "Demo User",
                "JASTIPER",
                "APPROVED",
                false,
                0L,
                4.5
        );
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userProfileMapper.toProfileResponse(user)).thenReturn(mapped);

        ProfileResponse response = authService.recordJastiperRating(1L, 5);

        assertEquals(2L, user.getJastiperRatingCount());
        assertEquals(9L, user.getJastiperRatingTotal());
        assertEquals(4.5, response.averageJastiperRating());
    }

    @Test
    void recordJastiperRatingShouldRejectOutOfRangeRatings() {
        assertEquals(400, assertThrows(ResponseStatusException.class,
                () -> authService.recordJastiperRating(1L, 0)).getStatusCode().value());
        assertEquals(400, assertThrows(ResponseStatusException.class,
                () -> authService.recordJastiperRating(1L, 6)).getStatusCode().value());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void recordJastiperRatingShouldHandleMissingCounters() {
        User user = sampleUser();
        user.setRole("JASTIPER");
        user.setJastiperRatingCount(null);
        user.setJastiperRatingTotal(null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userProfileMapper.toProfileResponse(user)).thenReturn(new ProfileResponse(
                1L,
                "user@example.com",
                "demo",
                "Demo User",
                "JASTIPER",
                "APPROVED",
                false,
                0L,
                5.0
        ));

        ProfileResponse response = authService.recordJastiperRating(1L, 5);

        assertEquals(1L, user.getJastiperRatingCount());
        assertEquals(5L, user.getJastiperRatingTotal());
        assertEquals(5.0, response.averageJastiperRating());
    }

    private static User sampleUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setUsername("demo");
        user.setFullName("Demo User");
        user.setRole("TITIPER");
        return user;
    }

    private static RegisterRequest registerRequest(String email, String password, String username) {
        RegisterRequest request = new RegisterRequest();
        request.setEmail(email);
        request.setPassword(password);
        request.setUsername(username);
        return request;
    }

    private static LoginRequest loginRequest(String email, String password) {
        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword(password);
        return request;
    }
}
