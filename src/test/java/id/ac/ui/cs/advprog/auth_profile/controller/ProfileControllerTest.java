package id.ac.ui.cs.advprog.auth_profile.controller;

import id.ac.ui.cs.advprog.auth_profile.dto.AdminUserActionRequest;
import id.ac.ui.cs.advprog.auth_profile.dto.ProfileResponse;
import id.ac.ui.cs.advprog.auth_profile.dto.ProfileUpdateRequest;
import id.ac.ui.cs.advprog.auth_profile.dto.RecordJastiperRatingRequest;
import id.ac.ui.cs.advprog.auth_profile.dto.SubmitKycRequest;
import id.ac.ui.cs.advprog.auth_profile.service.AuthService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProfileControllerTest {

    @Test
    void getProfileShouldDelegateToService() {
        AuthService service = mock(AuthService.class);
        ProfileResponse response = new ProfileResponse(7L, "user@example.com", "demo", "Demo", "TITIPER");
        when(service.getProfile(7L)).thenReturn(response);

        var entity = new ProfileController(service).getProfile(7L);

        assertEquals(response, entity.getBody());
    }

    @Test
    void updateProfileShouldUseAuthenticatedUserId() {
        AuthService service = mock(AuthService.class);
        ProfileUpdateRequest request = new ProfileUpdateRequest("demo", "Demo");
        ProfileResponse response = new ProfileResponse(7L, "user@example.com", "demo", "Demo", "TITIPER");
        when(service.updateProfile(7L, request)).thenReturn(response);

        var entity = new ProfileController(service)
                .updateProfile(new UsernamePasswordAuthenticationToken("7", null), request);

        assertEquals(response, entity.getBody());
    }

    @Test
    void submitKycShouldUseAuthenticatedUserId() {
        AuthService service = mock(AuthService.class);
        SubmitKycRequest request = new SubmitKycRequest("https://docs.example/kyc.pdf", "ready");
        ProfileResponse response = new ProfileResponse(7L, "user@example.com", "demo", "Demo", "TITIPER", "PENDING", false);
        when(service.submitKyc(7L, request)).thenReturn(response);

        var entity = new ProfileController(service)
                .submitKyc(new UsernamePasswordAuthenticationToken("7", null), request);

        assertEquals(response, entity.getBody());
    }

    @Test
    void adminEndpointsShouldRequireAdminRole() {
        AuthService service = mock(AuthService.class);
        ProfileController controller = new ProfileController(service);
        var nonAdmin = new UsernamePasswordAuthenticationToken("7", null, List.of(new SimpleGrantedAuthority("ROLE_TITIPER")));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> controller.listUsers(nonAdmin));

        assertEquals(403, exception.getStatusCode().value());
    }

    @Test
    void adminShouldListUsersAndModerateKyc() {
        AuthService service = mock(AuthService.class);
        ProfileController controller = new ProfileController(service);
        var admin = new UsernamePasswordAuthenticationToken("9001", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        ProfileResponse user = new ProfileResponse(7L, "user@example.com", "demo", "Demo", "TITIPER", "PENDING", false);
        ProfileResponse approved = new ProfileResponse(7L, "user@example.com", "demo", "Demo", "JASTIPER", "APPROVED", false);
        ProfileResponse rejected = new ProfileResponse(7L, "user@example.com", "demo", "Demo", "TITIPER", "REJECTED", false);

        when(service.listUsers()).thenReturn(List.of(user));
        when(service.approveKyc(7L, "ok")).thenReturn(approved);
        when(service.rejectKyc(7L, null)).thenReturn(rejected);

        assertEquals(1, controller.listUsers(admin).getBody().size());
        assertEquals("JASTIPER", controller.approveKyc(admin, 7L, new AdminUserActionRequest("ok")).getBody().role());
        assertEquals("REJECTED", controller.rejectKyc(admin, 7L, null).getBody().kycStatus());
    }

    @Test
    void adminShouldBanUnbanAndDemoteUsers() {
        AuthService service = mock(AuthService.class);
        ProfileController controller = new ProfileController(service);
        var admin = new UsernamePasswordAuthenticationToken("9001", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        ProfileResponse banned = new ProfileResponse(7L, "user@example.com", "demo", "Demo", "TITIPER", "PENDING", true);
        ProfileResponse unbanned = new ProfileResponse(7L, "user@example.com", "demo", "Demo", "TITIPER", "PENDING", false);
        ProfileResponse demoted = new ProfileResponse(7L, "user@example.com", "demo", "Demo", "TITIPER", "REJECTED", false);

        when(service.banUser(7L, "risk")).thenReturn(banned);
        when(service.unbanUser(7L)).thenReturn(unbanned);
        when(service.demoteJastiper(7L, null)).thenReturn(demoted);

        assertEquals(true, controller.banUser(admin, 7L, new AdminUserActionRequest("risk")).getBody().banned());
        assertEquals(false, controller.unbanUser(admin, 7L).getBody().banned());
        assertEquals("REJECTED", controller.demoteJastiper(admin, 7L, null).getBody().kycStatus());
    }

    @Test
    void adminEndpointsShouldHandleOptionalNotesBothWays() {
        AuthService service = mock(AuthService.class);
        ProfileController controller = new ProfileController(service);
        var admin = new UsernamePasswordAuthenticationToken("9001", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        ProfileResponse response = new ProfileResponse(7L, "user@example.com", "demo", "Demo", "TITIPER", "REJECTED", false);

        when(service.approveKyc(7L, null)).thenReturn(response);
        when(service.rejectKyc(7L, "no")).thenReturn(response);
        when(service.banUser(7L, null)).thenReturn(response);
        when(service.demoteJastiper(7L, "policy")).thenReturn(response);

        assertEquals(response, controller.approveKyc(admin, 7L, null).getBody());
        assertEquals(response, controller.rejectKyc(admin, 7L, new AdminUserActionRequest("no")).getBody());
        assertEquals(response, controller.banUser(admin, 7L, null).getBody());
        assertEquals(response, controller.demoteJastiper(admin, 7L, new AdminUserActionRequest("policy")).getBody());
    }

    @Test
    void adminGuardShouldRejectMissingAuthentication() {
        AuthService service = mock(AuthService.class);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> new ProfileController(service).listUsers(null)
        );

        assertEquals(403, exception.getStatusCode().value());
    }

    @Test
    void internalEndpointsShouldRecordJastiperStats() {
        AuthService service = mock(AuthService.class);
        ProfileController controller = new ProfileController(service);
        var internal = new UsernamePasswordAuthenticationToken(
                "internal-service",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_INTERNAL"))
        );
        ProfileResponse completed = new ProfileResponse(
                7L,
                "jastiper@example.com",
                "jastiper",
                "Jastiper",
                "JASTIPER",
                "APPROVED",
                false,
                3L,
                null
        );
        ProfileResponse rated = new ProfileResponse(
                7L,
                "jastiper@example.com",
                "jastiper",
                "Jastiper",
                "JASTIPER",
                "APPROVED",
                false,
                3L,
                4.5
        );

        when(service.recordJastiperCompletedOrder(7L)).thenReturn(completed);
        when(service.recordJastiperRating(7L, 5)).thenReturn(rated);

        assertEquals(3L, controller.recordJastiperCompletedOrder(internal, 7L).getBody().successfulTransactionCount());
        assertEquals(4.5, controller.recordJastiperRating(internal, 7L, new RecordJastiperRatingRequest(5))
                .getBody()
                .averageJastiperRating());
    }

    @Test
    void internalEndpointsShouldRejectNonInternalCaller() {
        AuthService service = mock(AuthService.class);
        ProfileController controller = new ProfileController(service);
        var buyer = new UsernamePasswordAuthenticationToken("7", null, List.of(new SimpleGrantedAuthority("ROLE_TITIPER")));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.recordJastiperCompletedOrder(buyer, 7L)
        );

        assertEquals(403, exception.getStatusCode().value());
    }

    @Test
    void internalEndpointsShouldRejectMissingAuthentication() {
        AuthService service = mock(AuthService.class);
        ProfileController controller = new ProfileController(service);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.recordJastiperCompletedOrder(null, 7L)
        );

        assertEquals(403, exception.getStatusCode().value());
    }
}
