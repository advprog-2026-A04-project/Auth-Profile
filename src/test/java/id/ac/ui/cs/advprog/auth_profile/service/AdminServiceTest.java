package id.ac.ui.cs.advprog.auth_profile.service;

import id.ac.ui.cs.advprog.auth_profile.dto.AdminUserResponse;
import id.ac.ui.cs.advprog.auth_profile.model.KycStatus;
import id.ac.ui.cs.advprog.auth_profile.model.User;
import id.ac.ui.cs.advprog.auth_profile.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AdminService adminService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("user@test.com");
        testUser.setUsername("testuser");
        testUser.setRole("TITIPERS");
        testUser.setKycStatus(KycStatus.NONE);
        testUser.setBanned(false);
    }

    @Test
    void getAllUsers_ReturnsPaginatedList() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(testUser), pageable, 1);
        when(userRepository.findAll(pageable)).thenReturn(userPage);

        Page<AdminUserResponse> result = adminService.getAllUsers(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("user@test.com", result.getContent().get(0).getEmail());
    }

    @Test
    void approveKyc_Success_RoleBecomesJastiper() {
        testUser.setKycStatus(KycStatus.PENDING);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        AdminUserResponse response = adminService.approveKyc(1L);

        assertEquals("JASTIPER", response.getRole());
        assertEquals("APPROVED", response.getKycStatus());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void approveKyc_NotPending_ThrowsException() {
        testUser.setKycStatus(KycStatus.NONE);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> adminService.approveKyc(1L));

        assertEquals("KYC is not in PENDING status", exception.getMessage());
    }

    @Test
    void rejectKyc_Success() {
        testUser.setKycStatus(KycStatus.PENDING);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        AdminUserResponse response = adminService.rejectKyc(1L);

        assertEquals("TITIPERS", response.getRole());
        assertEquals("REJECTED", response.getKycStatus());
    }

    @Test
    void banUser_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        AdminUserResponse response = adminService.banUser(1L);

        assertTrue(response.isBanned());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void banUser_AlreadyBanned_ThrowsException() {
        testUser.setBanned(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> adminService.banUser(1L));

        assertEquals("User is already banned", exception.getMessage());
    }

    @Test
    void updateRole_DemoteToTitipers_Success() {
        testUser.setRole("JASTIPER");
        testUser.setKycStatus(KycStatus.APPROVED);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        AdminUserResponse response = adminService.updateRole(1L, "TITIPERS");

        assertEquals("TITIPERS", response.getRole());
        assertEquals("NONE", response.getKycStatus());
    }

    @Test
    void updateRole_InvalidRole_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> adminService.updateRole(1L, "ADMIN"));

        assertEquals("Can only demote to TITIPERS", exception.getMessage());
    }
}
