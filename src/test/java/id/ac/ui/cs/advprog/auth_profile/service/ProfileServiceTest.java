package id.ac.ui.cs.advprog.auth_profile.service;

import id.ac.ui.cs.advprog.auth_profile.dto.KycRequest;
import id.ac.ui.cs.advprog.auth_profile.dto.ProfileResponse;
import id.ac.ui.cs.advprog.auth_profile.dto.ProfileUpdateRequest;
import id.ac.ui.cs.advprog.auth_profile.model.KycStatus;
import id.ac.ui.cs.advprog.auth_profile.model.User;
import id.ac.ui.cs.advprog.auth_profile.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProfileService profileService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@test.com");
        testUser.setUsername("testuser");
        testUser.setFullName("Test User");
        testUser.setRole("TITIPERS");
        testUser.setKycStatus(KycStatus.NONE);
    }

    // --- updateProfile tests ---

    @Test
    void updateProfile_Success() {
        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setUsername("newusername");
        request.setFullName("New Full Name");

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        when(userRepository.existsByUsername("newusername")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        ProfileResponse response = profileService.updateProfile("test@test.com", request);

        assertNotNull(response);
        assertEquals("newusername", response.getUsername());
        assertEquals("New Full Name", response.getFullName());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateProfile_UsernameAlreadyTaken_ThrowsException() {
        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setUsername("taken_username");

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        when(userRepository.existsByUsername("taken_username")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> profileService.updateProfile("test@test.com", request));

        assertEquals("Username already taken", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    // --- submitKyc tests ---

    @Test
    void submitKyc_Success() {
        KycRequest request = new KycRequest();
        request.setKycFullName("Omar Suyuf");
        request.setNik("1234567890123456");
        request.setPhoneNumber("08123456789");

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        ProfileResponse response = profileService.submitKyc("test@test.com", request);

        assertNotNull(response);
        assertEquals("PENDING", response.getKycStatus());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void submitKyc_AlreadyPending_ThrowsException() {
        testUser.setKycStatus(KycStatus.PENDING);

        KycRequest request = new KycRequest();
        request.setKycFullName("Omar Suyuf");
        request.setNik("1234567890123456");
        request.setPhoneNumber("08123456789");

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> profileService.submitKyc("test@test.com", request));

        assertEquals("KYC application is already pending", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void submitKyc_NikNot16Digits_ThrowsException() {
        KycRequest request = new KycRequest();
        request.setKycFullName("Omar Suyuf");
        request.setNik("12345"); // Not 16 digits
        request.setPhoneNumber("08123456789");

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> profileService.submitKyc("test@test.com", request));

        assertEquals("NIK must be exactly 16 digits", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void submitKyc_PhoneNumberInvalid_ThrowsException() {
        KycRequest request = new KycRequest();
        request.setKycFullName("Omar Suyuf");
        request.setNik("1234567890123456");
        request.setPhoneNumber("12345"); // Invalid phone

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> profileService.submitKyc("test@test.com", request));

        assertEquals("Phone number must start with 08 and be 10-13 digits", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }
}
