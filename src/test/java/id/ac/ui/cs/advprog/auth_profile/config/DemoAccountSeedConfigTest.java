package id.ac.ui.cs.advprog.auth_profile.config;

import id.ac.ui.cs.advprog.auth_profile.model.User;
import id.ac.ui.cs.advprog.auth_profile.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class DemoAccountSeedConfigTest {

    private DemoAccountSeedConfig config;
    private UserRepository userRepository;
    private JdbcTemplate jdbcTemplate;
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        config = new DemoAccountSeedConfig();
        userRepository = mock(UserRepository.class);
        jdbcTemplate = mock(JdbcTemplate.class);
        passwordEncoder = mock(PasswordEncoder.class);
    }

    @Test
    void ensureAccountShouldSkipWhenFixedIdAlreadyExists() {
        when(userRepository.findById(1001L)).thenReturn(Optional.of(new User()));

        invokeEnsureAccount();

        verifyNoInteractions(jdbcTemplate, passwordEncoder);
        verify(userRepository, never()).existsByEmail("demo@json.app");
        verify(userRepository, never()).existsByUsername("demo-buyer");
    }

    @Test
    void ensureAccountShouldSkipWhenEmailAlreadyExists() {
        when(userRepository.findById(1001L)).thenReturn(Optional.empty());
        when(userRepository.existsByEmail("demo@json.app")).thenReturn(true);

        invokeEnsureAccount();

        verifyNoInteractions(jdbcTemplate, passwordEncoder);
        verify(userRepository, never()).existsByUsername("demo-buyer");
    }

    @Test
    void ensureAccountShouldSkipWhenUsernameAlreadyExists() {
        when(userRepository.findById(1001L)).thenReturn(Optional.empty());
        when(userRepository.existsByEmail("demo@json.app")).thenReturn(false);
        when(userRepository.existsByUsername("demo-buyer")).thenReturn(true);

        invokeEnsureAccount();

        verifyNoInteractions(jdbcTemplate, passwordEncoder);
    }

    private void invokeEnsureAccount() {
        ReflectionTestUtils.invokeMethod(
                config,
                "ensureAccount",
                userRepository,
                jdbcTemplate,
                passwordEncoder,
                1001L,
                "demo@json.app",
                "demo-buyer",
                "Demo Buyer",
                "TITIPER"
        );
    }
}
